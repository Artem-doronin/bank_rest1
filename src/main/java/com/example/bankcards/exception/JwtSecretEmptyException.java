package com.example.bankcards.exception;

public class JwtSecretEmptyException extends RuntimeException {
    public JwtSecretEmptyException(String message) {
        super(message);
    }
}
