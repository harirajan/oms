package com.hari.oms.order.service;

import com.hari.oms.order.domain.Order;
import com.hari.oms.order.domain.OrderNotFoundException;
import com.hari.oms.order.domain.OrderRepository;
import com.hari.oms.order.domain.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order createOrder(UUID customerId, BigDecimal totalAmount, String currency) {
        Order order = Order.create(customerId, totalAmount, currency);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Transactional
    public Order cancelOrder(UUID orderId) {
        Order order = getOrder(orderId);
        order.transitionTo(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }
}