package com.hari.oms.order.api;

import com.hari.oms.order.domain.Order;
import com.hari.oms.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID customerId,
        OrderStatus status,
        BigDecimal totalAmount,
        String currency,
        Instant createdAt,
        Instant updatedAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}