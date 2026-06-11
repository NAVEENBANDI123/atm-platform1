package com.atm.dto.request;

import com.atm.entity.TicketStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TicketResolveRequest(
        @NotNull TicketStatus status,
        @Size(max = 2000) String resolution
) {
}
