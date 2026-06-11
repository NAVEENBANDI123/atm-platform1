package com.atm.exception;

import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends ApiException {

    public InsufficientBalanceException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
