package com.atm.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BalanceResponse(
        String accountNumber,
        BigDecimal balance,
        String currency
) {
}
