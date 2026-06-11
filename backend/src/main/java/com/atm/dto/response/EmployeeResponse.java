package com.atm.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

@Builder
public record EmployeeResponse(
        Long id,
        String employeeCode,
        String username,
        String email,
        String mobile,
        String fullName,
        String designation,
        String department,
        String status,
        boolean enabled,
        Set<String> roles,
        LocalDateTime createdAt
) {
}
