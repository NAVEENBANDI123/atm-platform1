package com.atm.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AuditLogResponse(
        Long id,
        String username,
        String userRole,
        String action,
        String entityType,
        String entityId,
        String details,
        String oldValue,
        String newValue,
        String ipAddress,
        LocalDateTime createdAt
) {
}
