package com.hari.oms.order.saga;

import com.hari.oms.order.domain.Order;
import com.hari.oms.order.domain.OrderRepository;
import com.hari.oms.order.domain.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SagaOrchestratorTest {

    @Mock
    private OrderRepository orderRepository;

    private FakeInventoryPort inventoryPort;
    private FakePaymentPort paymentPort;
    private FakeShippingPort shippingPort;
    private SagaOrchestrator orchestrator;

    private Order order;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        inventoryPort = new FakeInventoryPort();
        paymentPort = new FakePaymentPort();
        shippingPort = new FakeShippingPort();
        orchestrator = new SagaOrchestrator(orderRepository, inventoryPort, paymentPort, shippingPort);

        order = Order.create(UUID.randomUUID(), new BigDecimal("100.00"), "INR");
        orderId = order.getId();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void happyPathReachesShippedOnFirstAttempt() {
        Order result = orchestrator.processOrder(orderId);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(inventoryPort.wasReserved(orderId)).isTrue();
        assertThat(paymentPort.wasAuthorized(orderId)).isTrue();
        assertThat(shippingPort.wasShipped(orderId)).isTrue();
        assertThat(shippingPort.getAttemptCount()).isEqualTo(1);
    }

    @Test
    void inventoryReservationFailureCancelsImmediatelyNoCompensationNeeded() {
        inventoryPort.failNextReservation("out of stock");

        Order result = orchestrator.processOrder(orderId);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(paymentPort.wasAuthorized(orderId)).isFalse();
        assertThat(inventoryPort.wasReleased(orderId)).isFalse(); // nothing was reserved, nothing to release
    }

    @Test
    void paymentFailureReleasesInventoryAndCancels() {
        paymentPort.failNextAuthorization("card declined");

        Order result = orchestrator.processOrder(orderId);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(inventoryPort.wasReserved(orderId)).isTrue();
        assertThat(inventoryPort.wasReleased(orderId)).isTrue(); // compensation fired
        assertThat(paymentPort.wasRefunded(orderId)).isFalse();  // payment never succeeded, nothing to refund
    }

    @Test
    void shipmentSucceedsAfterTransientFailures() {
        shippingPort.failNextAttempts(2, "carrier timeout");

        Order result = orchestrator.processOrder(orderId);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(shippingPort.getAttemptCount()).isEqualTo(3);
        assertThat(paymentPort.wasRefunded(orderId)).isFalse();
    }

    @Test
    void shipmentExhaustsRetriesTriggersRefundAndCancellation() {
        shippingPort.failNextAttempts(5, "carrier down"); // more than MAX_SHIPMENT_ATTEMPTS

        Order result = orchestrator.processOrder(orderId);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(inventoryPort.wasReleased(orderId)).isTrue();
        assertThat(paymentPort.wasRefunded(orderId)).isTrue(); // money had moved, so refund path used, not bare cancel
        assertThat(shippingPort.getAttemptCount()).isEqualTo(3); // stopped at MAX_SHIPMENT_ATTEMPTS, didn't keep retrying forever
    }
}