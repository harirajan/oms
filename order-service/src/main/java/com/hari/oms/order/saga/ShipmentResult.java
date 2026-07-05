package com.hari.oms.order.saga;

public record ShipmentResult(boolean success, String failureReason) {

    public static ShipmentResult failure(String reason) {
        return new ShipmentResult(false, reason);
    }
}