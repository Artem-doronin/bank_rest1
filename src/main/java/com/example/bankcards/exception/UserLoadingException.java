package com.example.bankcards.exception;

public class UserLoadingException extends RuntimeException{
    public UserLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserLoadingException(String message) {
        super(message);
    }
}
