package com.hari.oms.order.saga;

import com.hari.oms.order.domain.Order;
import com.hari.oms.order.domain.OrderRepository;
import com.hari.oms.order.domain.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

//@Component
public class SagaOrchestrator {

    private static final int MAX_SHIPMENT_ATTEMPTS = 3;

    private final OrderRepository orderRepository;
    private final InventoryPort inventoryPort;
    private final PaymentPort paymentPort;
    private final ShippingPort shippingPort;

    public SagaOrchestrator(OrderRepository orderRepository,
                            InventoryPort inventoryPort,
                            PaymentPort paymentPort,
                            ShippingPort shippingPort) {
        this.orderRepository = orderRepository;
        this.inventoryPort = inventoryPort;
        this.paymentPort = paymentPort;
        this.shippingPort = shippingPort;
    }

    public Order processOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        ReservationResult reservation = inventoryPort.reserve(orderId);
        if (!reservation.success()) {
            order.transitionTo(OrderStatus.CANCELLED);
            return orderRepository.save(order);
        }
        order.transitionTo(OrderStatus.INVENTORY_RESERVED);
        orderRepository.save(order);

        PaymentResult payment = paymentPort.authorize(orderId, order.getTotalAmount(), order.getCurrency());
        if (!payment.success()) {
            inventoryPort.release(orderId);
            order.transitionTo(OrderStatus.CANCELLED);
            return orderRepository.save(order);
        }
        order.transitionTo(OrderStatus.PAYMENT_AUTHORIZED);
        orderRepository.save(order);

        order.transitionTo(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        for (int attempt = 1; attempt <= MAX_SHIPMENT_ATTEMPTS; attempt++) {
            ShipmentResult shipment = shippingPort.createShipment(orderId);
            if (shipment.success()) {
                order.transitionTo(OrderStatus.SHIPPED);
                return orderRepository.save(order);
            }
        }

        // Shipment retries exhausted: payment already moved, so we must unwind via refund, not a bare cancel
        inventoryPort.release(orderId);
        paymentPort.refund(orderId);
        order.transitionTo(OrderStatus.REFUND_INITIATED);
        orderRepository.save(order);
        order.transitionTo(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }
}