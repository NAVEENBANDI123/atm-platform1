package com.atm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ForgotPasswordRequest(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Mobile is required")
        @Pattern(regexp = "^[0-9]{10,15}$", message = "Mobile must be 10-15 digits")
        String mobile
) {
}
