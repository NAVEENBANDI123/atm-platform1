package com.atm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Reset token is required")
        String resetToken,

        @NotBlank
        @Size(min = 8, max = 72, message = "Password must be 8-72 characters")
        String newPassword
) {
}
