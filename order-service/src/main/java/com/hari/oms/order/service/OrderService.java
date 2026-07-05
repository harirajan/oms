package com.hari.oms.order.service;

import com.hari.oms.order.domain.Order;
import com.hari.oms.order.domain.OrderNotFoundException;
import com.hari.oms.order.domain.OrderRepository;
import com.hari.oms.order.domain.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order createOrder(UUID customerId, String currency, List<OrderLineRequest> lineItems) {
        Order order = Order.create(customerId, currency);
        for (OrderLineRequest line : lineItems) {
            order.addLine(line.sku(), line.quantity(), line.unitPrice());
        }
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getOrder(UUID orderId) {
        return orderRepository.findByIdWithLines(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Transactional
    public Order cancelOrder(UUID orderId) {
        Order order = getOrder(orderId);
        order.transitionTo(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    public record OrderLineRequest(String sku, int quantity, BigDecimal unitPrice) {
    }
}