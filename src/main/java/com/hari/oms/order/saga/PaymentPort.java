package com.hari.oms.order.saga;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentPort {

    PaymentResult authorize(UUID orderId, BigDecimal amount, String currency);

    void refund(UUID orderId);
}