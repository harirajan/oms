package com.hari.oms.order.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    void createStartsInCreatedStatusWithGeneratedIdAndZeroTotal() {
        Order order = Order.create(UUID.randomUUID(), "INR");

        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getCreatedAt()).isNotNull();
        assertThat(order.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(order.getLines()).isEmpty();
    }

    @Test
    void addingLinesRecalculatesTotal() {
        Order order = Order.create(UUID.randomUUID(), "INR");

        order.addLine("SKU-1", 2, new BigDecimal("25.00"));
        order.addLine("SKU-2", 1, new BigDecimal("49.99"));

        assertThat(order.getLines()).hasSize(2);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("99.99");
    }

    @Test
    void cannotAddLinesAfterOrderLeavesCreated() {
        Order order = Order.create(UUID.randomUUID(), "INR");
        order.addLine("SKU-1", 1, new BigDecimal("10.00"));
        order.transitionTo(OrderStatus.INVENTORY_RESERVED);

        assertThatThrownBy(() -> order.addLine("SKU-2", 1, new BigDecimal("5.00")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CREATED");
    }

    @Test
    void validTransitionUpdatesStatusAndTimestamp() {
        Order order = Order.create(UUID.randomUUID(), "INR");
        order.addLine("SKU-1", 1, new BigDecimal("50.00"));
        var originalUpdatedAt = order.getUpdatedAt();

        order.transitionTo(OrderStatus.INVENTORY_RESERVED);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.INVENTORY_RESERVED);
        assertThat(order.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
    }

    @Test
    void illegalTransitionThrows() {
        Order order = Order.create(UUID.randomUUID(), "INR");
        order.addLine("SKU-1", 1, new BigDecimal("50.00"));

        assertThatThrownBy(() -> order.transitionTo(OrderStatus.SHIPPED))
                .isInstanceOf(InvalidOrderStateTransitionException.class)
                .hasMessageContaining("CREATED")
                .hasMessageContaining("SHIPPED");
    }
}