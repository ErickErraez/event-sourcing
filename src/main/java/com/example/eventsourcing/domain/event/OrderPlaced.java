package com.example.eventsourcing.domain.event;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderPlaced(
        String orderId,
        String customerId,
        BigDecimal totalAmount,
        Instant occurredAt
) implements OrderEvent {
    @Override
    public String type() {
        return "OrderPlaced";
    }
}