package com.hari.oms.order.domain;

import com.hari.oms.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class OrderRepositoryIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void savesAndReloadsOrderWithAllFields() {
        Order order = Order.create(UUID.randomUUID(), new BigDecimal("249.50"), "INR");

        Order saved = orderRepository.save(order);
        orderRepository.flush();

        Optional<Order> reloaded = orderRepository.findById(saved.getId());

        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(reloaded.get().getTotalAmount()).isEqualByComparingTo("249.50");
        assertThat(reloaded.get().getCurrency()).isEqualTo("INR");
        assertThat(reloaded.get().getVersion()).isEqualTo(0L);
    }

    @Test
    void transitionPersistsAndIncrementsVersion() {
        Order order = Order.create(UUID.randomUUID(), new BigDecimal("75.00"), "INR");
        Order saved = orderRepository.saveAndFlush(order);
        Long versionAfterCreate = saved.getVersion();

        saved.transitionTo(OrderStatus.INVENTORY_RESERVED);
        Order updated = orderRepository.saveAndFlush(saved);

        assertThat(updated.getStatus()).isEqualTo(OrderStatus.INVENTORY_RESERVED);
        assertThat(updated.getVersion()).isGreaterThan(versionAfterCreate);
    }

    @Test
    void concurrentUpdatesTriggerOptimisticLockException() {
        Order order = Order.create(UUID.randomUUID(), new BigDecimal("120.00"), "INR");
        Order saved = orderRepository.saveAndFlush(order);

        // Simulate two callers loading the same row independently
        Order copy1 = orderRepository.findById(saved.getId()).orElseThrow();
        Order copy2 = orderRepository.findById(saved.getId()).orElseThrow();

        copy1.transitionTo(OrderStatus.INVENTORY_RESERVED);
        orderRepository.saveAndFlush(copy1);

        copy2.transitionTo(OrderStatus.CANCELLED);

        assertThatThrownBy(() -> orderRepository.saveAndFlush(copy2))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }
}