package com.hari.oms.order.saga;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FakePaymentPort implements PaymentPort {

    private boolean shouldFail = false;
    private String failureReason = "payment declined";
    private final Set<UUID> authorizedOrders = new HashSet<>();
    private final Set<UUID> refundedOrders = new HashSet<>();

    public void failNextAuthorization(String reason) {
        this.shouldFail = true;
        this.failureReason = reason;
    }

    @Override
    public PaymentResult authorize(UUID orderId, BigDecimal amount, String currency) {
        if (shouldFail) {
            return PaymentResult.failure(failureReason);
        }
        authorizedOrders.add(orderId);
        return new PaymentResult(true, null);
    }

    @Override
    public void refund(UUID orderId) {
        refundedOrders.add(orderId);
    }

    public boolean wasRefunded(UUID orderId) {
        return refundedOrders.contains(orderId);
    }

    public boolean wasAuthorized(UUID orderId) {
        return authorizedOrders.contains(orderId);
    }
}