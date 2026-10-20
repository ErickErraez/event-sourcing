package com.example.eventsourcing.infrastructure.persistence;

import com.example.eventsourcing.application.exception.ConcurrencyException;
import com.example.eventsourcing.application.port.out.EventStore;
import com.example.eventsourcing.domain.event.OrderEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryEventStore implements EventStore {
    private final Map<String, List<OrderEvent>> streams = new HashMap<>();

    @Override
    public synchronized List<OrderEvent> load(String orderId) {
        return List.copyOf(streams.getOrDefault(orderId, List.of()));
    }

    @Override
    public synchronized void append(String orderId, int expectedVersion, List<OrderEvent> events) {
        List<OrderEvent> stream = streams.computeIfAbsent(orderId, ignored -> new ArrayList<>());
        if (stream.size() != expectedVersion) {
            throw new ConcurrencyException(orderId, expectedVersion, stream.size());
        }
        if (events.isEmpty() || events.stream().anyMatch(event -> !orderId.equals(event.orderId()))) {
            throw new IllegalArgumentException("El append requiere eventos del mismo pedido.");
        }
        stream.addAll(events);
    }
}