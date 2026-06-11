package com.atm.service.impl;

import com.atm.audit.AuditService;
import com.atm.dto.request.CashierDepositRequest;
import com.atm.dto.request.CashierWithdrawRequest;
import com.atm.dto.response.AccountResponse;
import com.atm.dto.response.TransactionResponse;
import com.atm.entity.Account;
import com.atm.entity.AccountStatus;
import com.atm.entity.Transaction;
import com.atm.entity.TransactionType;
import com.atm.entity.User;
import com.atm.exception.BadRequestException;
import com.atm.exception.InsufficientBalanceException;
import com.atm.exception.ResourceNotFoundException;
import com.atm.mapper.AccountMapper;
import com.atm.mapper.TransactionMapper;
import com.atm.repository.AccountRepository;
import com.atm.repository.TransactionRepository;
import com.atm.service.CashierService;
import com.atm.service.EmailService;
import com.atm.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CashierServiceImpl implements CashierService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountMapper accountMapper;
    private final TransactionMapper transactionMapper;
    private final AuditService auditService;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public AccountResponse lookup(String accountNumber) {
        Account a = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountNumber));
        return accountMapper.toResponse(a, true);
    }

    @Override
    @Transactional
    public TransactionResponse deposit(CashierDepositRequest req, String operatorUsername) {
        Account account = accountRepository.findByAccountNumber(req.accountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        Account locked = lock(account.getId());
        ensureActive(locked);

        locked.setBalance(locked.getBalance().add(req.amount()));
        accountRepository.save(locked);

        Transaction tx = recordTx(locked, TransactionType.DEPOSIT, req.amount(),
                locked.getBalance(),
                description(req.description(), "Cash deposit by " + operatorUsername));

        auditService.recordWithValues("CASHIER_DEPOSIT", "ACCOUNT", String.valueOf(locked.getId()),
                "Operator " + operatorUsername + " deposited " + req.amount(),
                locked.getBalance().subtract(req.amount()).toPlainString(),
                locked.getBalance().toPlainString());

        notifyOwner(locked, tx, "Deposit credited", "deposit-credit",
                Map.of("amount", req.amount().toPlainString(),
                       "accountNumber", locked.getAccountNumber(),
                       "reference", tx.getReference(),
                       "balance", locked.getBalance().toPlainString()));

        return transactionMapper.toResponse(tx);
    }

    @Override
    @Transactional
    public TransactionResponse withdraw(CashierWithdrawRequest req, String operatorUsername) {
        Account account = accountRepository.findByAccountNumber(req.accountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        Account locked = lock(account.getId());
        ensureActive(locked);

        if (locked.getBalance().compareTo(req.amount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for this withdrawal");
        }
        locked.setBalance(locked.getBalance().subtract(req.amount()));
        accountRepository.save(locked);

        Transaction tx = recordTx(locked, TransactionType.WITHDRAWAL, req.amount(),
                locked.getBalance(),
                description(req.description(), "Cash withdrawal by " + operatorUsername));

        auditService.recordWithValues("CASHIER_WITHDRAW", "ACCOUNT", String.valueOf(locked.getId()),
                "Operator " + operatorUsername + " withdrew " + req.amount(),
                locked.getBalance().add(req.amount()).toPlainString(),
                locked.getBalance().toPlainString());

        notifyOwner(locked, tx, "Withdrawal debited", "withdrawal-debit",
                Map.of("amount", req.amount().toPlainString(),
                       "accountNumber", locked.getAccountNumber(),
                       "reference", tx.getReference(),
                       "balance", locked.getBalance().toPlainString()));

        return transactionMapper.toResponse(tx);
    }

    // ---------------------------------------------------------------------

    private Account lock(Long id) {
        return accountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    private void ensureActive(Account a) {
        if (a.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Account is " + a.getStatus());
        }
    }

    private Transaction recordTx(Account account, TransactionType type, BigDecimal amount,
                                 BigDecimal balanceAfter, String description) {
        Transaction tx = Transaction.builder()
                .reference("TXN-" + UUID.randomUUID().toString().replace("-", "")
                        .substring(0, 16).toUpperCase())
                .account(account)
                .type(type)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .description(description)
                .build();
        return transactionRepository.save(tx);
    }

    private void notifyOwner(Account account, Transaction tx, String notificationTitle,
                             String emailTemplate, Map<String, Object> vars) {
        User owner = account.getUser();
        if (owner == null) return;
        Map<String, Object> v = new HashMap<>(vars);
        v.put("name", owner.getFullName());
        emailService.send(owner.getEmail(),
                notificationTitle + ": " + vars.get("amount"),
                emailTemplate, v);
        notificationService.push(owner, notificationTitle,
                vars.get("amount") + " - reference " + tx.getReference());
    }

    private static String description(String provided, String fallback) {
        return (provided == null || provided.isBlank()) ? fallback : provided;
    }
}
