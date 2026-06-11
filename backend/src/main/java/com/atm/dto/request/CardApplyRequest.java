package com.atm.dto.request;

import com.atm.entity.CardType;
import jakarta.validation.constraints.NotNull;

public record CardApplyRequest(
        @NotNull CardType cardType
) {
}
