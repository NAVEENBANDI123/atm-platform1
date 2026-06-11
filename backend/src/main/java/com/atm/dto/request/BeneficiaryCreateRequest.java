package com.atm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BeneficiaryCreateRequest(
        @Size(max = 60) String nickname,
        @NotBlank @Pattern(regexp = "^[0-9]{6,20}$",
                message = "Account number must be 6-20 digits") String accountNumber,
        @NotBlank @Size(max = 120) String beneficiaryName,
        @Size(max = 120) String bankName,
        @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$",
                message = "IFSC must be 11 chars, e.g. SBIN0001234") String ifsc
) {
}
