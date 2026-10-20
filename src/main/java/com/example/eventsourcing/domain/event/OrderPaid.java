package com.example.eventsourcing.domain.event;

import java.time.Instant;

public record OrderPaid(
        String orderId,
        String paymentMethod,
        Instant occurredAt
) implements OrderEvent {
    @Override
    public String type() {
        return "OrderPaid";
    }
}