package com.atm.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record CardResponse(
        Long id,
        String maskedNumber,
        String cardType,
        String status,
        LocalDate expiryDate,
        BigDecimal dailyLimit,
        String accountNumber
) {
}
