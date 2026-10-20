package com.example.eventsourcing.domain.model;

import com.example.eventsourcing.domain.event.OrderEvent;
import com.example.eventsourcing.domain.exception.InvalidOrderOperationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {
    @Test
    void rebuildsCurrentStateByReplayingEvents() {
        Order placed = Order.place("order-1", "customer-1", new BigDecimal("125.50"));
        List<OrderEvent> history = new ArrayList<>(placed.uncommittedEvents());

        Order rehydrated = Order.rehydrate(history);
        rehydrated.pay("CARD");
        history.addAll(rehydrated.uncommittedEvents());

        Order rebuilt = Order.rehydrate(history);

        assertEquals(OrderStatus.PAID, rebuilt.status());
        assertEquals("CARD", rebuilt.paymentMethod());
        assertEquals(new BigDecimal("125.50"), rebuilt.totalAmount());
        assertEquals(2, history.size());
    }

    @Test
    void doesNotAllowPayingAnOrderTwice() {
        Order order = Order.place("order-1", "customer-1", new BigDecimal("125.50"));
        order.pay("CARD");

        assertThrows(InvalidOrderOperationException.class, () -> order.pay("CASH"));
    }
}