package com.hari.oms.order.saga;

public record PaymentResult(boolean success, String failureReason) {

    public static PaymentResult failure(String reason) {
        return new PaymentResult(false, reason);
    }
}