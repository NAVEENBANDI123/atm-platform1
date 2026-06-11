package com.atm.service.impl;

import com.atm.common.Constants;
import com.atm.common.PageResponse;
import com.atm.dto.response.TransactionResponse;
import com.atm.entity.Account;
import com.atm.entity.Transaction;
import com.atm.entity.User;
import com.atm.exception.ResourceNotFoundException;
import com.atm.mapper.TransactionMapper;
import com.atm.repository.AccountRepository;
import com.atm.repository.TransactionRepository;
import com.atm.repository.UserRepository;
import com.atm.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> getHistory(String username, Pageable pageable) {
        Account account = loadAccount(username);
        return PageResponse.from(
                transactionRepository.findByAccountIdOrderByCreatedAtDesc(account.getId(), pageable)
                        .map(transactionMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getMiniStatement(String username) {
        Account account = loadAccount(username);
        return transactionRepository.findTop5ByAccountIdOrderByCreatedAtDesc(account.getId())
                .stream()
                .limit(Constants.MINI_STATEMENT_SIZE)
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public String exportStatementCsv(String username) {
        Account account = loadAccount(username);
        Pageable pageable = PageRequest.of(0, 1000, Sort.by("createdAt").descending());
        List<Transaction> transactions = transactionRepository
                .findByAccountIdOrderByCreatedAtDesc(account.getId(), pageable).getContent();

        StringBuilder sb = new StringBuilder();
        sb.append("Date,Reference,Type,Amount,BalanceAfter,Description\n");
        for (Transaction t : transactions) {
            sb.append(t.getCreatedAt()).append(',')
              .append(escape(t.getReference())).append(',')
              .append(t.getType()).append(',')
              .append(t.getAmount()).append(',')
              .append(t.getBalanceAfter()).append(',')
              .append(escape(t.getDescription())).append('\n');
        }
        return sb.toString();
    }

    private Account loadAccount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No account found for this user"));
    }

    private static String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
