package com.compapptition.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando el usuario no tiene permisos suficientes para realizar la operación solicitada. Devuelve HTTP 401.
 *
 * @author Mario
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }

    /** Permite preservar la causa original al traducir excepciones técnicas (cierra A-20). */
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
