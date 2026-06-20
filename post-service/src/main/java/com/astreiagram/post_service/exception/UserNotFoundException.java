package com.astreiagram.post_service.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String id) {
        super("Usuário não encontrado: " + id);
    }
}
