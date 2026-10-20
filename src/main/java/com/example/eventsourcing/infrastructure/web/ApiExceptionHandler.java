package com.example.eventsourcing.infrastructure.web;

import com.example.eventsourcing.application.exception.ConcurrencyException;
import com.example.eventsourcing.application.exception.OrderNotFoundException;
import com.example.eventsourcing.domain.exception.InvalidOrderOperationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(OrderNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(OrderNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(exception.getMessage()));
    }

    @ExceptionHandler(InvalidOrderOperationException.class)
    ResponseEntity<ApiError> handleInvalidOperation(InvalidOrderOperationException exception) {
        return ResponseEntity.badRequest().body(new ApiError(exception.getMessage()));
    }

    @ExceptionHandler(ConcurrencyException.class)
    ResponseEntity<ApiError> handleConcurrency(ConcurrencyException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(exception.getMessage()));
    }

    public record ApiError(String message) {
    }
}