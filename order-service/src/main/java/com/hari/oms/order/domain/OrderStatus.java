package com.hari.oms.order.domain;

import java.util.Map;
import java.util.Set;

public enum OrderStatus {

    CREATED,
    INVENTORY_RESERVED,
    PAYMENT_AUTHORIZED,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    REFUND_INITIATED,
    CANCELLED;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            CREATED,             Set.of(INVENTORY_RESERVED, CANCELLED),
            INVENTORY_RESERVED,  Set.of(PAYMENT_AUTHORIZED, CANCELLED),
            PAYMENT_AUTHORIZED,  Set.of(CONFIRMED, CANCELLED),
            CONFIRMED,           Set.of(SHIPPED, REFUND_INITIATED),
            REFUND_INITIATED,    Set.of(CANCELLED),
            SHIPPED,             Set.of(DELIVERED),
            DELIVERED,           Set.of(),
            CANCELLED,           Set.of()
    );

    public boolean canTransitionTo(OrderStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }
}