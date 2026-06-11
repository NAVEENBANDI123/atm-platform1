package com.atm.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CardApplicationResponse(
        Long id,
        String applicantUsername,
        String applicantName,
        String accountNumber,
        String cardType,
        String status,
        String reviewNote,
        String rejectReason,
        LocalDateTime createdAt,
        LocalDateTime approvedAt,
        LocalDateTime rejectedAt
) {
}
