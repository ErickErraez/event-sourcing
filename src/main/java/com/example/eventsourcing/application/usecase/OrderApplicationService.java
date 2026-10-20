package com.example.eventsourcing.application.usecase;

import com.example.eventsourcing.application.exception.OrderNotFoundException;
import com.example.eventsourcing.application.port.out.EventStore;
import com.example.eventsourcing.domain.event.OrderEvent;
import com.example.eventsourcing.domain.model.Order;

import java.math.BigDecimal;
import java.util.List;

public class OrderApplicationService {
    private final EventStore eventStore;

    public OrderApplicationService(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    public Order placeOrder(String orderId, String customerId, BigDecimal totalAmount) {
        Order order = Order.place(orderId, customerId, totalAmount);
        eventStore.append(orderId, 0, order.uncommittedEvents());
        return order;
    }

    public Order payOrder(String orderId, String paymentMethod) {
        List<OrderEvent> history = eventStore.load(orderId);
        if (history.isEmpty()) {
            throw new OrderNotFoundException(orderId);
        }

        Order order = Order.rehydrate(history);
        order.pay(paymentMethod);
        eventStore.append(orderId, history.size(), order.uncommittedEvents());
        return order;
    }

    public Order getOrder(String orderId) {
        List<OrderEvent> history = getHistory(orderId);
        return Order.rehydrate(history);
    }

    public List<OrderEvent> getEvents(String orderId) {
        return getHistory(orderId);
    }

    private List<OrderEvent> getHistory(String orderId) {
        List<OrderEvent> history = eventStore.load(orderId);
        if (history.isEmpty()) {
            throw new OrderNotFoundException(orderId);
        }
        return history;
    }
}