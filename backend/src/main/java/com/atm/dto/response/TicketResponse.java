package com.atm.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TicketResponse(
        Long id,
        String customerUsername,
        String customerFullName,
        String subject,
        String description,
        String status,
        String resolution,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
