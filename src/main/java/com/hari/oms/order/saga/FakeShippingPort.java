package com.hari.oms.order.saga;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FakeShippingPort implements ShippingPort {

    private int failuresRemaining = 0;
    private String failureReason = "carrier unavailable";
    private final Set<UUID> shippedOrders = new HashSet<>();
    private int attemptCount = 0;

    public void failNextAttempts(int count, String reason) {
        this.failuresRemaining = count;
        this.failureReason = reason;
    }

    @Override
    public ShipmentResult createShipment(UUID orderId) {
        attemptCount++;
        if (failuresRemaining > 0) {
            failuresRemaining--;
            return ShipmentResult.failure(failureReason);
        }
        shippedOrders.add(orderId);
        return new ShipmentResult(true, null);
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public boolean wasShipped(UUID orderId) {
        return shippedOrders.contains(orderId);
    }
}