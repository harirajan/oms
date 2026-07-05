package com.hari.oms.order.saga;

public record ReservationResult(boolean success, String failureReason) {

    public static ReservationResult failure(String reason) {
        return new ReservationResult(false, reason);
    }
}