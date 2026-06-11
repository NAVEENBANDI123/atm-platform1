package com.atm.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record DepositProductResponse(
        Long id,
        String depositType,
        BigDecimal principal,
        BigDecimal interestRate,
        Integer tenureMonths,
        BigDecimal monthlyAmount,
        LocalDate maturityDate,
        BigDecimal maturityAmount,
        String status,
        LocalDateTime createdAt
) {
}
