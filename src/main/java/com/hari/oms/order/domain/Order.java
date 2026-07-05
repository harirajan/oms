package com.hari.oms.order.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected Order() {
        // required by JPA
    }

    public static Order create(UUID customerId, BigDecimal totalAmount, String currency) {
        Order order = new Order();
        order.id = UUID.randomUUID();
        order.customerId = customerId;
        order.status = OrderStatus.CREATED;
        order.totalAmount = totalAmount;
        order.currency = currency;
        Instant now = Instant.now();
        order.createdAt = now;
        order.updatedAt = now;
        return order;
    }

    public void transitionTo(OrderStatus target) {
        if (!this.status.canTransitionTo(target)) {
            throw new InvalidOrderStateTransitionException(this.status, target);
        }
        this.status = target;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getCurrency() { return currency; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}