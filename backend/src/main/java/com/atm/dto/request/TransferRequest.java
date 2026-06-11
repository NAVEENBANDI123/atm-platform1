package com.atm.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Customer fund-transfer request. Either {@link #beneficiaryId} (preferred,
 * uses the customer's saved & verified beneficiary) or
 * {@link #targetAccountNumber} must be supplied.
 */
public record TransferRequest(
        Long beneficiaryId,

        @Pattern(regexp = "^[0-9]{6,20}$", message = "Invalid target account number")
        String targetAccountNumber,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        @Digits(integer = 17, fraction = 2)
        BigDecimal amount,

        @Size(max = 255)
        String description
) {
    @AssertTrue(message = "Either beneficiaryId or targetAccountNumber must be supplied")
    public boolean isDestinationProvided() {
        return beneficiaryId != null
                || (targetAccountNumber != null && !targetAccountNumber.isBlank());
    }
}
