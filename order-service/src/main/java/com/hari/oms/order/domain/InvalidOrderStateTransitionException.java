package com.hari.oms.order.domain;

public class InvalidOrderStateTransitionException extends RuntimeException {

    public InvalidOrderStateTransitionException(OrderStatus from, OrderStatus to) {
        super("Cannot transition order from %s to %s".formatted(from, to));
    }
}