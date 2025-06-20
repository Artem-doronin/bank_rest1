package com.example.bankcards.exception;

public class CardAlreadyExistsException extends RuntimeException {
    public CardAlreadyExistsException(String message) {
        super(message);
    }

    public CardAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
