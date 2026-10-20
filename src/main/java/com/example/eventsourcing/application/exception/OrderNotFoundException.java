package com.example.eventsourcing.application.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String orderId) {
        super("No existe el pedido " + orderId + ".");
    }
}