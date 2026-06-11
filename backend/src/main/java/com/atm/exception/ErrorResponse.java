package com.atm.exception;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record ErrorResponse(
        boolean success,
        int status,
        String error,
        String message,
        List<String> errors,
        String path,
        Instant timestamp
) {
}
