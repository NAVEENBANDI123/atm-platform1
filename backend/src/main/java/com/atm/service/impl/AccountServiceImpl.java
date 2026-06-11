package com.atm.service.impl;

import com.atm.audit.AuditService;
import com.atm.dto.request.TransferRequest;
import com.atm.dto.response.AccountResponse;
import com.atm.dto.response.BalanceResponse;
import com.atm.dto.response.CustomerDashboardResponse;
import com.atm.dto.response.TransactionResponse;
import com.atm.entity.Account;
import com.atm.entity.AccountStatus;
import com.atm.entity.Beneficiary;
import com.atm.entity.CustomerProfile;
import com.atm.entity.Transaction;
import com.atm.entity.TransactionType;
import com.atm.entity.User;
import com.atm.exception.BadRequestException;
import com.atm.exception.InsufficientBalanceException;
import com.atm.exception.ResourceNotFoundException;
import com.atm.mapper.AccountMapper;
import com.atm.mapper.TransactionMapper;
import com.atm.repository.AccountRepository;
import com.atm.repository.BeneficiaryRepository;
import com.atm.repository.CustomerProfileRepository;
import com.atm.repository.NotificationRepository;
import com.atm.repository.TransactionRepository;
import com.atm.repository.UserRepository;
import com.atm.service.AccountService;
import com.atm.service.EmailService;
import com.atm.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final CustomerProfileRepository profileRepository;
    private final NotificationRepository notificationRepository;
    private final AccountMapper accountMapper;
    private final TransactionMapper transactionMapper;
    private final AuditService auditService;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getMyAccount(String username) {
        Account account = loadAccount(username);
        // balance is hidden in this default view
        return accountMapper.toResponse(account, false);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDashboardResponse getMyDashboard(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Account account = accountRepository.findByUserId(user.getId()).orElse(null);
        CustomerProfile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        long unread = notificationRepository.countByUserIdAndReadFalse(user.getId());
        return CustomerDashboardResponse.builder()
                .customerName(user.getFullName())
                .customerId(profile == null ? null : profile.getCustomerId())
                .accountNumber(account == null ? null : account.getAccountNumber())
                .accountType(account == null || account.getAccountType() == null
                        ? null : account.getAccountType().name())
                .accountStatus(account == null || account.getStatus() == null
                        ? null : account.getStatus().name())
                .currency(account == null ? null : account.getCurrency())
                .balance(null) // intentionally hidden
                .dailyTransferLimit(account == null ? null : account.getDailyTransferLimit())
                .unreadNotifications(unread)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceResponse getMyBalance(String username) {
        Account a = loadAccount(username);
        auditService.record("VIEW_BALANCE", "ACCOUNT", String.valueOf(a.getId()), null);
        return BalanceResponse.builder()
                .accountNumber(a.getAccountNumber())
                .balance(a.getBalance())
                .currency(a.getCurrency())
                .build();
    }

    @Override
    @Transactional
    public TransactionResponse transfer(String username, TransferRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Account source = accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found"));

        // Resolve target either via beneficiary or raw account number.
        String targetAccountNumber;
        Beneficiary beneficiary = null;
        if (request.beneficiaryId() != null) {
            beneficiary = beneficiaryRepository.findByIdAndOwnerId(
                            request.beneficiaryId(), user.getId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Beneficiary",
                            "id", request.beneficiaryId()));
            if (!beneficiary.isVerified()) {
                throw new BadRequestException("Beneficiary is not yet verified");
            }
            targetAccountNumber = beneficiary.getAccountNumber();
        } else {
            targetAccountNumber = request.targetAccountNumber();
        }

        Account target = accountRepository.findByAccountNumber(targetAccountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Target account " + targetAccountNumber + " not found"));

        if (source.getId().equals(target.getId())) {
            throw new BadRequestException("Cannot transfer to your own account");
        }

        // Lock both accounts in deterministic order to avoid deadlocks.
        Account first;
        Account second;
        if (source.getId() < target.getId()) {
            first = lock(source.getId());
            second = lock(target.getId());
        } else {
            second = lock(source.getId());
            first = lock(target.getId());
        }
        Account lockedSource = first.getId().equals(source.getId()) ? first : second;
        Account lockedTarget = first.getId().equals(target.getId()) ? first : second;

        ensureActive(lockedSource);
        ensureActive(lockedTarget);

        if (lockedSource.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for this transfer");
        }

        // Daily transfer limit enforcement (per-account).
        BigDecimal todayTotal = transactionRepository.sumByAccountAndTypeSince(
                lockedSource.getId(), TransactionType.TRANSFER_OUT,
                LocalDate.now().atStartOfDay());
        BigDecimal projected = todayTotal.add(request.amount());
        if (projected.compareTo(lockedSource.getDailyTransferLimit()) > 0) {
            throw new BadRequestException("This transfer would exceed your daily limit of "
                    + lockedSource.getDailyTransferLimit());
        }

        lockedSource.setBalance(lockedSource.getBalance().subtract(request.amount()));
        lockedTarget.setBalance(lockedTarget.getBalance().add(request.amount()));
        accountRepository.save(lockedSource);
        accountRepository.save(lockedTarget);

        String desc = description(request.description(),
                "Transfer to " + lockedTarget.getAccountNumber());
        Transaction outTx = recordTransaction(lockedSource, lockedTarget,
                TransactionType.TRANSFER_OUT, request.amount(), lockedSource.getBalance(), desc);
        recordTransaction(lockedTarget, lockedSource, TransactionType.TRANSFER_IN,
                request.amount(), lockedTarget.getBalance(),
                "Transfer from " + lockedSource.getAccountNumber());

        auditService.recordWithValues("FUND_TRANSFER", "TRANSACTION", outTx.getReference(),
                "From " + lockedSource.getAccountNumber() + " to " + lockedTarget.getAccountNumber()
                        + " amount " + request.amount(),
                lockedSource.getBalance().add(request.amount()).toPlainString(),
                lockedSource.getBalance().toPlainString());

        // Email + notification (async)
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", user.getFullName());
        vars.put("amount", request.amount().toPlainString());
        vars.put("targetAccount", lockedTarget.getAccountNumber());
        vars.put("reference", outTx.getReference());
        vars.put("balance", lockedSource.getBalance().toPlainString());
        emailService.send(user.getEmail(),
                "Transfer successful: " + request.amount() + " sent",
                "transfer-debit", vars);
        notificationService.push(user, "Transfer sent",
                "Sent " + request.amount() + " to " + lockedTarget.getAccountNumber()
                        + ".  Reference: " + outTx.getReference());

        // Notify the recipient if they're our customer too
        if (lockedTarget.getUser() != null) {
            User recipient = lockedTarget.getUser();
            Map<String, Object> recVars = new HashMap<>();
            recVars.put("name", recipient.getFullName());
            recVars.put("amount", request.amount().toPlainString());
            recVars.put("sourceAccount", lockedSource.getAccountNumber());
            recVars.put("reference", outTx.getReference());
            emailService.send(recipient.getEmail(),
                    "Credit alert: " + request.amount() + " received",
                    "transfer-credit", recVars);
            notificationService.push(recipient, "Funds received",
                    "Received " + request.amount() + " from " + lockedSource.getAccountNumber());
        }

        return transactionMapper.toResponse(outTx);
    }

    // ----------------------------------------------------------------------

    private Account loadAccount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No account found for this user"));
    }

    private Account lock(Long id) {
        return accountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    private void ensureActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Account is " + account.getStatus()
                    + " and cannot perform this operation");
        }
    }

    private Transaction recordTransaction(Account account, Account counterparty, TransactionType type,
                                          BigDecimal amount, BigDecimal balanceAfter, String description) {
        Transaction tx = Transaction.builder()
                .reference("TXN-" + UUID.randomUUID().toString().replace("-", "")
                        .substring(0, 16).toUpperCase())
                .account(account)
                .counterpartyAccount(counterparty)
                .type(type)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .description(description)
                .build();
        return transactionRepository.save(tx);
    }

    private String description(String provided, String fallback) {
        return (provided == null || provided.isBlank()) ? fallback : provided;
    }
}
