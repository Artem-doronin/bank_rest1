package com.example.bankcards.exception;

public class JwtValidityInvalidException extends RuntimeException {
    public JwtValidityInvalidException(String message) {
        super(message);
    }
}
