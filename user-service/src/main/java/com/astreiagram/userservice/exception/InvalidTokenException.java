package com.astreiagram.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Lançada quando um token JWT é inválido, expirado ou mal formado.
 * Resulta em HTTP 401 (Unauthorized) — o cliente não está autenticado corretamente.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
