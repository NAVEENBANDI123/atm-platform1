package com.atm.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NotificationResponse(
        Long id,
        String title,
        String body,
        boolean read,
        LocalDateTime createdAt
) {
}
