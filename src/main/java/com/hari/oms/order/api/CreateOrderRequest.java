package com.hari.oms.order.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderRequest(

        @NotNull(message = "customerId is required")
        UUID customerId,

        @NotNull(message = "totalAmount is required")
        @DecimalMin(value = "0.01", message = "totalAmount must be greater than zero")
        BigDecimal totalAmount,

        @NotNull(message = "currency is required")
        @Size(min = 3, max = 3, message = "currency must be a 3-letter ISO code")
        String currency
) {
}