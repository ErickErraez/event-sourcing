package com.example.eventsourcing.infrastructure.web;

import com.example.eventsourcing.application.usecase.OrderApplicationService;
import com.example.eventsourcing.domain.event.OrderEvent;
import com.example.eventsourcing.domain.event.OrderPaid;
import com.example.eventsourcing.domain.event.OrderPlaced;
import com.example.eventsourcing.domain.model.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderApplicationService orderService;

    public OrderController(OrderApplicationService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody PlaceOrderRequest request) {
        Order order = orderService.placeOrder(request.orderId(), request.customerId(), request.totalAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
    }

    @PostMapping("/{orderId}/payments")
    public OrderResponse payOrder(@PathVariable String orderId, @RequestBody PayOrderRequest request) {
        return OrderResponse.from(orderService.payOrder(orderId, request.paymentMethod()));
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable String orderId) {
        return OrderResponse.from(orderService.getOrder(orderId));
    }

    @GetMapping("/{orderId}/events")
    public List<EventResponse> getEvents(@PathVariable String orderId) {
        List<OrderEvent> events = orderService.getEvents(orderId);
        return java.util.stream.IntStream.range(0, events.size())
                .mapToObj(index -> EventResponse.from(index + 1, events.get(index)))
                .toList();
    }

    public record PlaceOrderRequest(String orderId, String customerId, BigDecimal totalAmount) {
    }

    public record PayOrderRequest(String paymentMethod) {
    }

    public record OrderResponse(
            String orderId,
            String customerId,
            BigDecimal totalAmount,
            String paymentMethod,
            String status
    ) {
        static OrderResponse from(Order order) {
            return new OrderResponse(order.orderId(), order.customerId(), order.totalAmount(),
                    order.paymentMethod(), order.status().name());
        }
    }

    public record EventResponse(int version, String type, String orderId, String occurredAt, Map<String, Object> data) {
        static EventResponse from(int version, OrderEvent event) {
            Map<String, Object> data;
            if (event instanceof OrderPlaced placed) {
                data = Map.of("customerId", placed.customerId(), "totalAmount", placed.totalAmount());
            } else if (event instanceof OrderPaid paid) {
                data = Map.of("paymentMethod", paid.paymentMethod());
            } else {
                throw new IllegalArgumentException("Tipo de evento no soportado.");
            }
            return new EventResponse(version, event.type(), event.orderId(), event.occurredAt().toString(), data);
        }
    }
}