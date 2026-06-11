package com.atm.mapper;

import com.atm.dto.response.AccountResponse;
import com.atm.entity.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountResponse toResponse(Account account) {
        return toResponse(account, true);
    }

    /**
     * Maps an {@link Account} to a response. When {@code includeBalance} is
     * {@code false} the balance field is set to {@code null} so the customer
     * dashboard can mask it ("********").
     */
    public AccountResponse toResponse(Account account, boolean includeBalance) {
        if (account == null) {
            return null;
        }
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .ownerName(account.getUser() == null ? null : account.getUser().getFullName())
                .balance(includeBalance ? account.getBalance() : null)
                .currency(account.getCurrency())
                .status(account.getStatus())
                .accountType(account.getAccountType())
                .dailyTransferLimit(account.getDailyTransferLimit())
                .build();
    }
}
