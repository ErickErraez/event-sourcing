package com.example.eventsourcing.application.port.out;

import com.example.eventsourcing.domain.event.OrderEvent;

import java.util.List;

public interface EventStore {
    List<OrderEvent> load(String orderId);

    void append(String orderId, int expectedVersion, List<OrderEvent> events);
}