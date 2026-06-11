package com.atm.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record NomineeResponse(
        Long id,
        String accountNumber,
        String name,
        String relationship,
        LocalDate dateOfBirth,
        BigDecimal sharePercent
) {
}
