package com.hari.oms.order.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(

        @NotNull(message = "customerId is required")
        UUID customerId,

        @NotNull(message = "currency is required")
        @Size(min = 3, max = 3, message = "currency must be a 3-letter ISO code")
        String currency,

        @NotEmpty(message = "at least one order line is required")
        @Valid
        List<OrderLineRequest> lines
) {
        public record OrderLineRequest(

                @NotNull(message = "sku is required")
                String sku,

                @Positive(message = "quantity must be positive")
                int quantity,

                @NotNull(message = "unitPrice is required")
                @DecimalMin(value = "0.01", message = "unitPrice must be greater than zero")
                BigDecimal unitPrice
        ) {
        }
}