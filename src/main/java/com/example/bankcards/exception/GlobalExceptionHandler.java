package com.example.bankcards.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({
            CardNotFoundException.class,
            UserNotFoundException.class,
            EntityNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(Exception ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(NOT_FOUND).body(toErrorResponse(ex, NOT_FOUND));
    }

    @ExceptionHandler({
            CardAlreadyExistsException.class,
            IllegalArgumentException.class

    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.status(BAD_REQUEST).body(toErrorResponse(ex, BAD_REQUEST));
    }

    @ExceptionHandler({
            CardBlockedException.class,
            InsufficientFundsException.class,
    })
    public ResponseEntity<ErrorResponse> handleConflict(Exception ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return ResponseEntity.status(CONFLICT).body(toErrorResponse(ex, CONFLICT));
    }

    @ExceptionHandler({
            NullPointerException.class,
            IllegalStateException.class
    })
    public ResponseEntity<ErrorResponse> handleInternalServerError(Exception ex) {
        log.error("Internal server error (500): ", ex);  // Логируем полный stack trace
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(toErrorResponse(ex, INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleJwtAuthException(JwtAuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(UNAUTHORIZED).body(toErrorResponse(ex, UNAUTHORIZED));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        log.error("Unhandled exception occurred: ", ex);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(toErrorResponse(ex, INTERNAL_SERVER_ERROR));
    }

    private ErrorResponse toErrorResponse(Exception ex, HttpStatus status) {
        return ErrorResponse.create(ex, status, ex.getMessage());
    }
}
