package com.compapptition.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando la petición del cliente contiene datos inválidos o incumple reglas de negocio. Devuelve HTTP 400.
 *
 * @author Mario
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }

    /** Permite preservar la causa original al traducir excepciones técnicas (cierra A-20). */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
