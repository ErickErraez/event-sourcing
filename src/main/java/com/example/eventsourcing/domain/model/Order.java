package com.example.eventsourcing.domain.model;

import com.example.eventsourcing.domain.event.OrderEvent;
import com.example.eventsourcing.domain.event.OrderPaid;
import com.example.eventsourcing.domain.event.OrderPlaced;
import com.example.eventsourcing.domain.exception.InvalidOrderOperationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class Order {
    private String orderId;
    private String customerId;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private OrderStatus status;
    private final List<OrderEvent> uncommittedEvents = new ArrayList<>();

    private Order() {
    }

    public static Order place(String orderId, String customerId, BigDecimal totalAmount) {
        requireText(orderId, "orderId");
        requireText(customerId, "customerId");
        if (totalAmount == null || totalAmount.signum() <= 0) {
            throw new InvalidOrderOperationException("El total debe ser mayor que cero.");
        }

        Order order = new Order();
        order.raise(new OrderPlaced(orderId, customerId, totalAmount, Instant.now()));
        return order;
    }

    public static Order rehydrate(List<OrderEvent> history) {
        if (history == null || history.isEmpty()) {
            throw new InvalidOrderOperationException("No se puede reconstruir un pedido sin eventos.");
        }

        Order order = new Order();
        history.forEach(order::apply);
        return order;
    }

    public void pay(String paymentMethod) {
        if (status != OrderStatus.PLACED) {
            throw new InvalidOrderOperationException("Solo se puede pagar un pedido en estado PLACED.");
        }
        requireText(paymentMethod, "paymentMethod");
        raise(new OrderPaid(orderId, paymentMethod, Instant.now()));
    }

    private void raise(OrderEvent event) {
        apply(event);
        uncommittedEvents.add(event);
    }

    private void apply(OrderEvent event) {
        if (event instanceof OrderPlaced placed) {
            if (status != null) {
                throw new InvalidOrderOperationException("El pedido ya fue creado.");
            }
            orderId = placed.orderId();
            customerId = placed.customerId();
            totalAmount = placed.totalAmount();
            status = OrderStatus.PLACED;
        } else if (event instanceof OrderPaid paid) {
            if (status != OrderStatus.PLACED || !orderId.equals(paid.orderId())) {
                throw new InvalidOrderOperationException("La secuencia de eventos del pedido no es valida.");
            }
            paymentMethod = paid.paymentMethod();
            status = OrderStatus.PAID;
        } else {
            throw new InvalidOrderOperationException("Tipo de evento de pedido no soportado.");
        }
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidOrderOperationException(field + " es obligatorio.");
        }
    }

    public List<OrderEvent> uncommittedEvents() {
        return List.copyOf(uncommittedEvents);
    }

    public String orderId() {
        return orderId;
    }

    public String customerId() {
        return customerId;
    }

    public BigDecimal totalAmount() {
        return totalAmount;
    }

    public String paymentMethod() {
        return paymentMethod;
    }

    public OrderStatus status() {
        return status;
    }
}