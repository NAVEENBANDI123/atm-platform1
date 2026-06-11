package com.atm.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base type for all domain exceptions. Carries the HTTP status to be returned.
 */
@Getter
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
