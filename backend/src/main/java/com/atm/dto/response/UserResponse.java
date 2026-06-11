package com.atm.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

@Builder
public record UserResponse(
        Long id,
        String username,
        String email,
        String mobile,
        String fullName,
        boolean enabled,
        boolean accountLocked,
        String userType,
        String status,
        String customerId,
        String employeeCode,
        Set<String> roles,
        LocalDateTime createdAt
) {
}
