package com.atm.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record LoanAccountResponse(
        Long id,
        String loanAccountNo,
        String creditedToAccountNumber,
        BigDecimal principal,
        BigDecimal interestRate,
        Integer tenureMonths,
        BigDecimal emiAmount,
        BigDecimal outstanding,
        LocalDateTime disbursedAt,
        List<LoanScheduleEntryResponse> schedule
) {
}
