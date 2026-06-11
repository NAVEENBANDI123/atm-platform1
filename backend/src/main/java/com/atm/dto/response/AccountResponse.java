package com.atm.dto.response;

import com.atm.entity.AccountStatus;
import com.atm.entity.AccountType;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * Account summary as returned by both customer and employee APIs.
 *
 * <p>The {@link #balance} field may be {@code null} when the customer
 * dashboard requests a "masked" view; clients render
 * {@code ********} until the user clicks "Show Balance".</p>
 */
@Builder
public record AccountResponse(
        Long id,
        String accountNumber,
        String ownerName,
        BigDecimal balance,
        String currency,
        AccountStatus status,
        AccountType accountType,
        BigDecimal dailyTransferLimit
) {
}
