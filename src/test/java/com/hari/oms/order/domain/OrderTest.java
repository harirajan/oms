package com.hari.oms.order.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    void createStartsInCreatedStatusWithGeneratedId() {
        Order order = Order.create(UUID.randomUUID(), new BigDecimal("199.99"), "INR");

        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getCreatedAt()).isNotNull();
    }

    @Test
    void validTransitionUpdatesStatusAndTimestamp() {
        Order order = Order.create(UUID.randomUUID(), new BigDecimal("50.00"), "INR");
        var originalUpdatedAt = order.getUpdatedAt();

        order.transitionTo(OrderStatus.INVENTORY_RESERVED);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.INVENTORY_RESERVED);
        assertThat(order.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
    }

    @Test
    void illegalTransitionThrows() {
        Order order = Order.create(UUID.randomUUID(), new BigDecimal("50.00"), "INR");

        assertThatThrownBy(() -> order.transitionTo(OrderStatus.SHIPPED))
                .isInstanceOf(InvalidOrderStateTransitionException.class)
                .hasMessageContaining("CREATED")
                .hasMessageContaining("SHIPPED");
    }
}