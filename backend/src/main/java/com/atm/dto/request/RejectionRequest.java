package com.atm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Generic rejection payload reused by customer / card / loan reject endpoints.
 */
public record RejectionRequest(
        @NotBlank @Size(min = 3, max = 500) String reason
) {
}
