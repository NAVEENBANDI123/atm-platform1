package com.atm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TicketCreateRequest(
        @NotBlank @Size(max = 160) String subject,
        @NotBlank @Size(max = 2000) String description
) {
}
