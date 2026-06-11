package com.atm.service.impl;

import com.atm.audit.AuditService;
import com.atm.common.PageResponse;
import com.atm.config.AppProperties;
import com.atm.dto.request.LoanApplyRequest;
import com.atm.dto.request.RejectionRequest;
import com.atm.dto.request.ReviewRequest;
import com.atm.dto.response.LoanAccountResponse;
import com.atm.dto.response.LoanApplicationResponse;
import com.atm.entity.Account;
import com.atm.entity.ApplicationStatus;
import com.atm.entity.LoanAccount;
import com.atm.entity.LoanApplication;
import com.atm.entity.LoanScheduleEntry;
import com.atm.entity.LoanType;
import com.atm.entity.Transaction;
import com.atm.entity.TransactionType;
import com.atm.exception.BadRequestException;
import com.atm.exception.ResourceNotFoundException;
import com.atm.entity.User;
import com.atm.mapper.DomainMapper;
import com.atm.repository.AccountRepository;
import com.atm.repository.LoanAccountRepository;
import com.atm.repository.LoanApplicationRepository;
import com.atm.repository.LoanScheduleRepository;
import com.atm.repository.TransactionRepository;
import com.atm.repository.UserRepository;
import com.atm.service.EmailService;
import com.atm.service.LoanService;
import com.atm.service.NotificationService;
import com.atm.util.EmiCalculator;
import com.atm.util.IdentifierGenerators;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanApplicationRepository applicationRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final LoanScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final IdentifierGenerators identifiers;
    private final AppProperties appProperties;
    private final AuditService auditService;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final DomainMapper mapper;

    @Override
    @Transactional
    public LoanApplicationResponse apply(String username, LoanApplyRequest req) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException(
                        "An active savings account is required before applying for a loan"));

        // Eligibility: monthly EMI must not exceed 50% of declared monthly income.
        BigDecimal rate = rateForType(req.loanType());
        BigDecimal emi = EmiCalculator.monthlyEmi(req.amount(), rate, req.tenureMonths());
        BigDecimal half = req.monthlyIncome().multiply(new BigDecimal("0.5"));
        if (emi.compareTo(half) > 0) {
            throw new BadRequestException(
                    "EMI " + emi + " exceeds 50% of your declared monthly income.  "
                    + "Reduce the amount or increase the tenure.");
        }

        LoanApplication app = LoanApplication.builder()
                .user(user)
                .loanType(req.loanType())
                .amount(req.amount())
                .tenureMonths(req.tenureMonths())
                .monthlyIncome(req.monthlyIncome())
                .employmentType(req.employmentType())
                .employerName(req.employerName())
                .purpose(req.purpose())
                .status(ApplicationStatus.PENDING)
                .build();
        applicationRepository.save(app);

        auditService.record("LOAN_APPLY", "LOAN_APPLICATION", String.valueOf(app.getId()),
                "Customer " + username + " applied for a " + req.loanType() + " loan of " + req.amount());

        emailService.send(user.getEmail(),
                "Loan application received",
                "loan-application-submitted",
                Map.of("name", user.getFullName(), "loanType", req.loanType().name(),
                        "amount", req.amount().toPlainString(),
                        "tenureMonths", req.tenureMonths(),
                        "estimatedEmi", emi.toPlainString()));

        emailService.send("loan.officer@atm.local",
                "New loan application from " + user.getUsername(),
                "officer-new-loan-request",
                Map.of("name", user.getFullName(),
                        "loanType", req.loanType().name(),
                        "amount", req.amount().toPlainString(),
                        "applicationId", app.getId()));
        return mapper.toLoanApplicationResponse(app);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> myApplications(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return applicationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), Pageable.unpaged())
                .stream()
                .map(mapper::toLoanApplicationResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanAccountResponse> myLoans(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return loanAccountRepository.findByAccountUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::toLoanAccountResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LoanApplicationResponse> listPending(Pageable pageable) {
        return PageResponse.from(applicationRepository.findByStatusInOrderByCreatedAtAsc(
                List.of(ApplicationStatus.PENDING, ApplicationStatus.UNDER_REVIEW), pageable)
                .map(mapper::toLoanApplicationResponse));
    }

    @Override
    @Transactional
    public LoanApplicationResponse review(Long applicationId, ReviewRequest req, String reviewerUsername) {
        LoanApplication app = mustApp(applicationId);
        if (app.getStatus() != ApplicationStatus.PENDING && app.getStatus() != ApplicationStatus.UNDER_REVIEW) {
            throw new BadRequestException("Application is in status " + app.getStatus());
        }
        User reviewer = userRepository.findByUsername(reviewerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found"));
        String previous = app.getStatus().name();
        app.setReviewedBy(reviewer.getId());
        app.setReviewNote(req.note());
        app.setStatus(req.recommendation() == ReviewRequest.Recommendation.RECOMMEND
                ? ApplicationStatus.UNDER_REVIEW
                : ApplicationStatus.PENDING);
        applicationRepository.save(app);

        auditService.recordWithValues("LOAN_REVIEW", "LOAN_APPLICATION", String.valueOf(app.getId()),
                "Reviewed by " + reviewerUsername + ": " + req.recommendation(),
                previous, app.getStatus().name());
        return mapper.toLoanApplicationResponse(app);
    }

    @Override
    @Transactional
    public LoanAccountResponse approve(Long applicationId, String approverUsername) {
        LoanApplication app = mustApp(applicationId);
        if (app.getStatus() != ApplicationStatus.UNDER_REVIEW && app.getStatus() != ApplicationStatus.PENDING) {
            throw new BadRequestException("Application status is " + app.getStatus());
        }
        User approver = userRepository.findByUsername(approverUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));

        // Generate loan account + repayment schedule
        BigDecimal rate = rateForType(app.getLoanType());
        BigDecimal emi = EmiCalculator.monthlyEmi(app.getAmount(), rate, app.getTenureMonths());

        String loanNumber;
        int tries = 0;
        do {
            loanNumber = identifiers.generateLoanAccountNo();
            tries++;
            if (tries > 20) throw new IllegalStateException("Could not allocate loan account number");
        } while (loanAccountRepository.existsByLoanAccountNo(loanNumber));

        Account targetAccount = accountRepository.findByUserId(app.getUser().getId())
                .orElseThrow(() -> new BadRequestException("Customer has no active savings account"));

        LoanAccount loanAccount = LoanAccount.builder()
                .application(app)
                .account(targetAccount)
                .loanAccountNo(loanNumber)
                .principal(app.getAmount())
                .interestRate(rate)
                .tenureMonths(app.getTenureMonths())
                .emiAmount(emi)
                .outstanding(app.getAmount())
                .disbursedAt(LocalDateTime.now())
                .build();
        loanAccountRepository.save(loanAccount);

        // Reducing-balance schedule
        BigDecimal monthlyRate = EmiCalculator.monthlyRate(rate);
        BigDecimal balance = app.getAmount();
        LocalDate due = LocalDate.now().plusMonths(1);
        for (int i = 1; i <= app.getTenureMonths(); i++) {
            BigDecimal interestPart = balance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPart = emi.subtract(interestPart);
            if (i == app.getTenureMonths()) {
                principalPart = balance; // close the loan exactly on the last installment
            }
            balance = balance.subtract(principalPart).max(BigDecimal.ZERO);
            scheduleRepository.save(LoanScheduleEntry.builder()
                    .loanAccount(loanAccount)
                    .installmentNo(i)
                    .dueDate(due)
                    .emiAmount(emi)
                    .principalPart(principalPart.setScale(2, RoundingMode.HALF_UP))
                    .interestPart(interestPart)
                    .balance(balance.setScale(2, RoundingMode.HALF_UP))
                    .paid(false)
                    .build());
            due = due.plusMonths(1);
        }

        // Credit the loan amount to the customer's savings account (atomically with locking).
        Account locked = accountRepository.findByIdForUpdate(targetAccount.getId())
                .orElseThrow(() -> new IllegalStateException("Account vanished"));
        locked.setBalance(locked.getBalance().add(app.getAmount()));
        accountRepository.save(locked);

        Transaction tx = Transaction.builder()
                .reference("LOAN-" + UUID.randomUUID().toString().replace("-", "")
                        .substring(0, 16).toUpperCase())
                .account(locked)
                .type(TransactionType.DEPOSIT)
                .amount(app.getAmount())
                .balanceAfter(locked.getBalance())
                .description("Loan disbursement: " + loanNumber)
                .build();
        transactionRepository.save(tx);

        String previous = app.getStatus().name();
        app.setStatus(ApplicationStatus.APPROVED);
        app.setApprovedBy(approver.getId());
        app.setApprovedAt(LocalDateTime.now());
        applicationRepository.save(app);

        auditService.recordWithValues("LOAN_APPROVE", "LOAN_ACCOUNT", String.valueOf(loanAccount.getId()),
                "Approved loan " + app.getId() + " for " + app.getAmount(),
                previous, "APPROVED");

        emailService.send(app.getUser().getEmail(),
                "Your loan has been approved",
                "loan-approved",
                Map.of(
                        "name", app.getUser().getFullName(),
                        "loanType", app.getLoanType().name(),
                        "amount", app.getAmount().toPlainString(),
                        "loanAccountNo", loanNumber,
                        "emiAmount", emi.toPlainString(),
                        "interestRate", rate.toPlainString()));
        notificationService.push(app.getUser(), "Loan approved",
                "Loan account " + loanNumber + " is active. Funds credited to "
                        + locked.getAccountNumber() + ".");

        return toLoanAccountResponse(loanAccount);
    }

    @Override
    @Transactional
    public LoanApplicationResponse reject(Long applicationId, RejectionRequest req, String approverUsername) {
        LoanApplication app = mustApp(applicationId);
        if (app.getStatus() == ApplicationStatus.APPROVED) {
            throw new BadRequestException("Cannot reject an already approved loan application");
        }
        User approver = userRepository.findByUsername(approverUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));

        String previous = app.getStatus().name();
        app.setStatus(ApplicationStatus.REJECTED);
        app.setApprovedBy(approver.getId());
        app.setRejectReason(req.reason());
        app.setRejectedAt(LocalDateTime.now());
        applicationRepository.save(app);

        auditService.recordWithValues("LOAN_REJECT", "LOAN_APPLICATION", String.valueOf(app.getId()),
                "Rejected: " + req.reason(),
                previous, "REJECTED");

        emailService.send(app.getUser().getEmail(),
                "Your loan application was not approved",
                "loan-rejected",
                Map.of("name", app.getUser().getFullName(),
                        "loanType", app.getLoanType().name(),
                        "amount", app.getAmount().toPlainString(),
                        "reason", req.reason()));
        notificationService.push(app.getUser(), "Loan application rejected",
                "Reason: " + req.reason());
        return mapper.toLoanApplicationResponse(app);
    }

    // -----------------------------------------------------------------

    private LoanApplication mustApp(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("LoanApplication", "id", id));
    }

    private BigDecimal rateForType(LoanType type) {
        return switch (type) {
            case PERSONAL  -> appProperties.getBanking().getLoanInterest().getPersonal();
            case EDUCATION -> appProperties.getBanking().getLoanInterest().getEducation();
            case VEHICLE   -> appProperties.getBanking().getLoanInterest().getVehicle();
            case HOME      -> appProperties.getBanking().getLoanInterest().getHome();
        };
    }

    private LoanAccountResponse toLoanAccountResponse(LoanAccount la) {
        List<LoanScheduleEntry> schedule = scheduleRepository
                .findByLoanAccountIdOrderByInstallmentNoAsc(la.getId());
        List<com.atm.dto.response.LoanScheduleEntryResponse> mappedSchedule = new ArrayList<>();
        for (LoanScheduleEntry e : schedule) {
            mappedSchedule.add(mapper.toLoanScheduleEntry(e));
        }
        return LoanAccountResponse.builder()
                .id(la.getId())
                .loanAccountNo(la.getLoanAccountNo())
                .creditedToAccountNumber(la.getAccount() == null ? null : la.getAccount().getAccountNumber())
                .principal(la.getPrincipal())
                .interestRate(la.getInterestRate())
                .tenureMonths(la.getTenureMonths())
                .emiAmount(la.getEmiAmount())
                .outstanding(la.getOutstanding())
                .disbursedAt(la.getDisbursedAt())
                .schedule(mappedSchedule)
                .build();
    }
}
