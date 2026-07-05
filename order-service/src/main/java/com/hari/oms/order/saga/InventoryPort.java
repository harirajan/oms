package com.hari.oms.order.saga;

import java.util.UUID;

public interface InventoryPort {

    ReservationResult reserve(UUID orderId);

    void release(UUID orderId);
}