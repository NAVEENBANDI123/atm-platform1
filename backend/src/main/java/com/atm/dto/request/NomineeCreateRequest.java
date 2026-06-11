package com.atm.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NomineeCreateRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 40) String relationship,
        @Past LocalDate dateOfBirth,
        @DecimalMin("0.01") @DecimalMax("100.00") BigDecimal sharePercent
) {
}
