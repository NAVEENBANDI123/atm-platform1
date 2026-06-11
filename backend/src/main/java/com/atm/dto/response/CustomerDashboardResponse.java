package com.atm.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * Customer-only dashboard summary.  By design the {@code balance} field
 * is {@code null} unless the customer explicitly clicks "Show Balance" on
 * the UI - the SPA renders {@code ********} otherwise.
 */
@Builder
public record CustomerDashboardResponse(
        String customerName,
        String customerId,
        String accountNumber,
        String accountType,
        String accountStatus,
        String currency,
        BigDecimal balance,
        BigDecimal dailyTransferLimit,
        long unreadNotifications
) {
}
