package com.hari.oms.order.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @Test
    void createdCanMoveToInventoryReservedOrCancelled() {
        assertThat(OrderStatus.CREATED.canTransitionTo(OrderStatus.INVENTORY_RESERVED)).isTrue();
        assertThat(OrderStatus.CREATED.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
    }

    @Test
    void createdCannotSkipStraightToConfirmed() {
        assertThat(OrderStatus.CREATED.canTransitionTo(OrderStatus.CONFIRMED)).isFalse();
    }

    @Test
    void shippedCanOnlyMoveToDelivered() {
        assertThat(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.DELIVERED)).isTrue();
        assertThat(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
    }

    @Test
    void confirmedCannotCancelDirectlyMustGoThroughRefund() {
        assertThat(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
        assertThat(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.REFUND_INITIATED)).isTrue();
    }

    @Test
    void refundInitiatedCanOnlyMoveToCancelled() {
        assertThat(OrderStatus.REFUND_INITIATED.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        assertThat(OrderStatus.REFUND_INITIATED.canTransitionTo(OrderStatus.SHIPPED)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    void terminalStatesHaveNoOutgoingTransitions(OrderStatus status) {
        if (status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED) {
            for (OrderStatus target : OrderStatus.values()) {
                assertThat(status.canTransitionTo(target))
                        .as("%s should not transition to %s", status, target)
                        .isFalse();
            }
        }
    }
}