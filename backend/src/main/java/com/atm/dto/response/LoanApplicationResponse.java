package com.atm.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record LoanApplicationResponse(
        Long id,
        String applicantUsername,
        String applicantName,
        String loanType,
        BigDecimal amount,
        Integer tenureMonths,
        BigDecimal monthlyIncome,
        String employmentType,
        String employerName,
        String purpose,
        String status,
        String reviewNote,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime approvedAt,
        LocalDateTime rejectedAt
) {
}
