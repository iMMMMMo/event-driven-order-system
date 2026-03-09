package com.example.ordersystem.order;

import com.example.ordersystem.config.AbstractIntegrationTest;
import com.example.ordersystem.order.domain.Order;
import com.example.ordersystem.order.domain.OrderStatus;
import com.example.ordersystem.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldCreateOrder() {
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        BigDecimal amount = new BigDecimal("100.00");

        Order order = Order.create(userId, email, amount);
        Order saved = orderRepository.save(order);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.CREATED);
    }
}