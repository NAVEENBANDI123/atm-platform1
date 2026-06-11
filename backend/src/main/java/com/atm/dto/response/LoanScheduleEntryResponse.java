package com.atm.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record LoanScheduleEntryResponse(
        Integer installmentNo,
        LocalDate dueDate,
        BigDecimal emiAmount,
        BigDecimal principalPart,
        BigDecimal interestPart,
        BigDecimal balance,
        boolean paid
) {
}
