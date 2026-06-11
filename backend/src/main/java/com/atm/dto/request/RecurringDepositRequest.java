package com.atm.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RecurringDepositRequest(
        @NotNull @DecimalMin("100.00") @Digits(integer = 17, fraction = 2) BigDecimal monthlyAmount,
        @NotNull @Min(6) @Max(120) Integer tenureMonths
) {
}
