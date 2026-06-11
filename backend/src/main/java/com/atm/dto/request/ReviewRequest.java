package com.atm.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Officer (CARD_OFFICER / LOAN_OFFICER) review payload. They can either
 * RECOMMEND the application for SUPER_ADMIN approval or RETURN it to the
 * customer with notes.
 */
public record ReviewRequest(
        @NotNull Recommendation recommendation,
        @Size(max = 500) String note
) {
    public enum Recommendation { RECOMMEND, RETURN }
}
