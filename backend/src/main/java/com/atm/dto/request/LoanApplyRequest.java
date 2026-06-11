package com.atm.dto.request;

import com.atm.entity.LoanType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record LoanApplyRequest(
        @NotNull LoanType loanType,
        @NotNull @DecimalMin(value = "1000.00", message = "Loan amount must be at least 1000")
        @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @NotNull @Min(6) @Max(360) Integer tenureMonths,
        @NotNull @DecimalMin(value = "1000.00", message = "Monthly income must be provided")
        @Digits(integer = 17, fraction = 2) BigDecimal monthlyIncome,
        @NotBlank @Size(max = 60) String employmentType,
        @Size(max = 120) String employerName,
        @NotBlank @Size(max = 500) String purpose
) {
}
