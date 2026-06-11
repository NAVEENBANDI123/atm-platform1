package com.atm.service.impl;

import com.atm.audit.AuditService;
import com.atm.config.AppProperties;
import com.atm.dto.request.FixedDepositRequest;
import com.atm.dto.request.RecurringDepositRequest;
import com.atm.dto.response.DepositProductResponse;
import com.atm.entity.Account;
import com.atm.entity.AccountStatus;
import com.atm.entity.DepositProduct;
import com.atm.entity.DepositType;
import com.atm.entity.Transaction;
import com.atm.entity.TransactionType;
import com.atm.entity.User;
import com.atm.exception.BadRequestException;
import com.atm.exception.InsufficientBalanceException;
import com.atm.exception.ResourceNotFoundException;
import com.atm.mapper.DomainMapper;
import com.atm.repository.AccountRepository;
import com.atm.repository.DepositProductRepository;
import com.atm.repository.TransactionRepository;
import com.atm.repository.UserRepository;
import com.atm.service.DepositProductService;
import com.atm.service.EmailService;
import com.atm.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepositProductServiceImpl implements DepositProductService {

    private final DepositProductRepository repository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AppProperties appProperties;
    private final AuditService auditService;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final DomainMapper mapper;

    @Override
    @Transactional
    public DepositProductResponse openFd(String username, FixedDepositRequest req) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Account account = lockUserAccount(user.getId());

        if (account.getBalance().compareTo(req.principal()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance to open this fixed deposit");
        }

        BigDecimal rate = appProperties.getBanking().getFdInterestRate();
        // Simple interest projection: principal + (P * R * T / 100), T in years
        BigDecimal years = new BigDecimal(req.tenureMonths()).divide(new BigDecimal("12"),
                6, RoundingMode.HALF_UP);
        BigDecimal interest = req.principal().multiply(rate).multiply(years)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal maturityAmount = req.principal().add(interest).setScale(2, RoundingMode.HALF_UP);

        DepositProduct fd = DepositProduct.builder()
                .user(user)
                .account(account)
                .depositType(DepositType.FIXED)
                .principal(req.principal())
                .interestRate(rate)
                .tenureMonths(req.tenureMonths())
                .maturityDate(LocalDate.now().plusMonths(req.tenureMonths()))
                .maturityAmount(maturityAmount)
                .status("ACTIVE")
                .build();
        repository.save(fd);

        // Debit principal from savings account
        account.setBalance(account.getBalance().subtract(req.principal()));
        accountRepository.save(account);
        Transaction tx = Transaction.builder()
                .reference("FD-" + UUID.randomUUID().toString().replace("-", "")
                        .substring(0, 16).toUpperCase())
                .account(account)
                .type(TransactionType.WITHDRAWAL)
                .amount(req.principal())
                .balanceAfter(account.getBalance())
                .description("Fixed deposit booking #" + fd.getId())
                .build();
        transactionRepository.save(tx);

        auditService.record("OPEN_FD", "DEPOSIT", String.valueOf(fd.getId()),
                "FD of " + req.principal() + " for " + req.tenureMonths() + " months");

        emailService.send(user.getEmail(),
                "Your fixed deposit is active",
                "fd-opened",
                Map.of(
                        "name", user.getFullName(),
                        "principal", req.principal().toPlainString(),
                        "rate", rate.toPlainString(),
                        "tenureMonths", req.tenureMonths(),
                        "maturityAmount", maturityAmount.toPlainString(),
                        "maturityDate", fd.getMaturityDate().toString()));
        notificationService.push(user, "Fixed deposit opened",
                "Maturity " + maturityAmount + " on " + fd.getMaturityDate());
        return mapper.toDepositProductResponse(fd);
    }

    @Override
    @Transactional
    public DepositProductResponse openRd(String username, RecurringDepositRequest req) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Account account = lockUserAccount(user.getId());
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Account is not active");
        }
        BigDecimal rate = appProperties.getBanking().getRdInterestRate();

        // Approximate maturity = monthly * months * (1 + rate * months / 24 * 100)
        BigDecimal months = new BigDecimal(req.tenureMonths());
        BigDecimal totalContributed = req.monthlyAmount().multiply(months);
        BigDecimal interest = totalContributed
                .multiply(rate)
                .multiply(months.add(BigDecimal.ONE))
                .divide(new BigDecimal("2400"), 2, RoundingMode.HALF_UP);
        BigDecimal maturityAmount = totalContributed.add(interest).setScale(2, RoundingMode.HALF_UP);

        DepositProduct rd = DepositProduct.builder()
                .user(user)
                .account(account)
                .depositType(DepositType.RECURRING)
                .principal(totalContributed)
                .monthlyAmount(req.monthlyAmount())
                .interestRate(rate)
                .tenureMonths(req.tenureMonths())
                .maturityDate(LocalDate.now().plusMonths(req.tenureMonths()))
                .maturityAmount(maturityAmount)
                .status("ACTIVE")
                .build();
        repository.save(rd);

        auditService.record("OPEN_RD", "DEPOSIT", String.valueOf(rd.getId()),
                "RD of " + req.monthlyAmount() + "/m for " + req.tenureMonths() + " months");

        emailService.send(user.getEmail(),
                "Your recurring deposit is active",
                "rd-opened",
                Map.of(
                        "name", user.getFullName(),
                        "monthlyAmount", req.monthlyAmount().toPlainString(),
                        "rate", rate.toPlainString(),
                        "tenureMonths", req.tenureMonths(),
                        "maturityAmount", maturityAmount.toPlainString(),
                        "maturityDate", rd.getMaturityDate().toString()));
        notificationService.push(user, "Recurring deposit opened",
                "Monthly " + req.monthlyAmount() + " - maturity " + maturityAmount);
        return mapper.toDepositProductResponse(rd);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepositProductResponse> myDeposits(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return repository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(mapper::toDepositProductResponse)
                .toList();
    }

    private Account lockUserAccount(Long userId) {
        Account a = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("No active account"));
        return accountRepository.findByIdForUpdate(a.getId())
                .orElseThrow(() -> new BadRequestException("Account locked / unavailable"));
    }
}
