package com.hari.oms.order.api;

import com.hari.oms.order.domain.Order;
import com.hari.oms.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(
                request.customerId(), request.totalAmount(), request.currency());

        return ResponseEntity
                .created(URI.create("/orders/" + order.getId()))
                .body(OrderResponse.from(order));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
        Order order = orderService.getOrder(orderId);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable UUID orderId) {
        Order order = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(OrderResponse.from(order));
    }
}