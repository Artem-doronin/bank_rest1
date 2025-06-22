package com.example.bankcards.exception;

public class InvalidCardIdException extends RuntimeException {
    public InvalidCardIdException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidCardIdException(String message) {
        super(message);
    }
}
