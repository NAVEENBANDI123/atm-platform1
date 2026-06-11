package com.atm.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public static ResourceNotFoundException of(String resource, String field, Object value) {
        return new ResourceNotFoundException("%s not found with %s: %s".formatted(resource, field, value));
    }
}
