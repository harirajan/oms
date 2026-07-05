package com.hari.oms.order.saga;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FakeInventoryPort implements InventoryPort {

    private boolean shouldFail = false;
    private String failureReason = "inventory unavailable";
    private final Set<UUID> reservedOrders = new HashSet<>();
    private final Set<UUID> releasedOrders = new HashSet<>();

    public void failNextReservation(String reason) {
        this.shouldFail = true;
        this.failureReason = reason;
    }

    @Override
    public ReservationResult reserve(UUID orderId) {
        if (shouldFail) {
            return ReservationResult.failure(failureReason);
        }
        reservedOrders.add(orderId);
        return new ReservationResult(true, null);
    }

    @Override
    public void release(UUID orderId) {
        releasedOrders.add(orderId);
    }

    public boolean wasReleased(UUID orderId) {
        return releasedOrders.contains(orderId);
    }

    public boolean wasReserved(UUID orderId) {
        return reservedOrders.contains(orderId);
    }
}