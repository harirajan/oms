package com.hari.oms.order.saga;

import java.util.UUID;

public interface ShippingPort {

    ShipmentResult createShipment(UUID orderId);
}