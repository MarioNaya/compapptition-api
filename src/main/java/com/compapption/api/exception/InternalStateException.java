package com.compapption.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalStateException extends RuntimeException {
    public InternalStateException(String message) {
        super(message);
    }
}
