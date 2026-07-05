package com.hari.oms.order.domain;

import com.hari.oms.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;

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
    void savesAndReloadsOrderWithLinesAndComputedTotal() {
        Order order = Order.create(UUID.randomUUID(), "INR");
        order.addLine("SKU-249", 1, new BigDecimal("249.50"));

        Order saved = orderRepository.save(order);
        orderRepository.flush();

        Optional<Order> reloaded = orderRepository.findByIdWithLines(saved.getId());

        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(reloaded.get().getTotalAmount()).isEqualByComparingTo("249.50");
        assertThat(reloaded.get().getLines()).hasSize(1);
        assertThat(reloaded.get().getLines().get(0).getSku()).isEqualTo("SKU-249");
    }

    @Test
    void transitionPersistsAndIncrementsVersion() {
        Order order = Order.create(UUID.randomUUID(), "INR");
        order.addLine("SKU-75", 1, new BigDecimal("75.00"));
        Order saved = orderRepository.saveAndFlush(order);
        Long versionAfterCreate = saved.getVersion();

        saved.transitionTo(OrderStatus.INVENTORY_RESERVED);
        Order updated = orderRepository.saveAndFlush(saved);

        assertThat(updated.getStatus()).isEqualTo(OrderStatus.INVENTORY_RESERVED);
        assertThat(updated.getVersion()).isGreaterThan(versionAfterCreate);
    }

    @Test
    void concurrentUpdatesTriggerOptimisticLockException() {
        Order order = Order.create(UUID.randomUUID(), "INR");
        order.addLine("SKU-120", 1, new BigDecimal("120.00"));
        Order saved = orderRepository.saveAndFlush(order);

        Order copy1 = orderRepository.findById(saved.getId()).orElseThrow();
        Order copy2 = orderRepository.findById(saved.getId()).orElseThrow();

        copy1.transitionTo(OrderStatus.INVENTORY_RESERVED);
        orderRepository.saveAndFlush(copy1);

        copy2.transitionTo(OrderStatus.CANCELLED);

        assertThatThrownBy(() -> orderRepository.saveAndFlush(copy2))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }
}