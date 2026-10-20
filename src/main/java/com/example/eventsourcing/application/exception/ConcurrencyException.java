package com.example.eventsourcing.application.exception;

public class ConcurrencyException extends RuntimeException {
    public ConcurrencyException(String orderId, int expectedVersion, int actualVersion) {
        super("Conflicto en el stream " + orderId + ": version esperada " + expectedVersion
                + ", version actual " + actualVersion + ".");
    }
}