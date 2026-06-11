package com.atm.dto.response;

import lombok.Builder;

@Builder
public record CustomerRegistrationResponse(
        Long userId,
        String username,
        String email,
        String status,
        String message
) {
}
