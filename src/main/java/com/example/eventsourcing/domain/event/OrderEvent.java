package com.example.eventsourcing.domain.event;

import java.time.Instant;

public interface OrderEvent {
    String orderId();

    Instant occurredAt();

    String type();
}