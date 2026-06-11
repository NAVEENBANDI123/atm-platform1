package com.atm.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BeneficiaryResponse(
        Long id,
        String nickname,
        String accountNumber,
        String beneficiaryName,
        String bankName,
        String ifsc,
        boolean verified,
        LocalDateTime createdAt
) {
}
