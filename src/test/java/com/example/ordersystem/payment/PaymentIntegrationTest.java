package com.example.ordersystem.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.ordersystem.config.AbstractIntegrationTest;
import com.example.ordersystem.order.domain.Order;
import com.example.ordersystem.order.domain.OrderStatus;
import com.example.ordersystem.order.repository.OrderRepository;
import com.example.ordersystem.payment.domain.Payment;
import com.example.ordersystem.payment.domain.PaymentStatus;
import com.example.ordersystem.payment.repository.PaymentRepository;
import com.example.ordersystem.payment.service.PaymentService;
import com.example.ordersystem.user.domain.User;
import com.example.ordersystem.user.domain.UserRole;
import com.example.ordersystem.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PaymentIntegrationTest extends AbstractIntegrationTest {

  @Autowired private PaymentService paymentService;

  @Autowired private PaymentRepository paymentRepository;

  @Autowired private OrderRepository orderRepository;

  @Autowired private UserRepository userRepository;

  private User testUser;

  @BeforeEach
  void resetState() {
    paymentRepository.deleteAll();
    orderRepository.deleteAll();
    userRepository.deleteAll();
    testUser =
        userRepository.save(
            User.builder()
                .email("payment-test-" + UUID.randomUUID() + "@example.com")
                .password("password")
                .role(UserRole.USER)
                .build());
  }

  @Test
  void shouldPersistSuccessfulPaymentAndMarkOrderAsPaid() {
    Order order =
        orderRepository.save(
            Order.create(
                testUser.getId(), "payment-success@example.com", new BigDecimal("100.00")));

    paymentService.processPayment(
        order.getId(), new BigDecimal("100.00"), new BigDecimal("100.00"));

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              Payment payment = paymentRepository.findAll().getFirst();
              Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();

              assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
              assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
            });
  }

  @Test
  void shouldPersistFailedPaymentAndCancelOrderWhenAmountDoesNotMatch() {
    Order order =
        orderRepository.save(
            Order.create(
                testUser.getId(), "payment-failure@example.com", new BigDecimal("100.00")));

    paymentService.processPayment(order.getId(), new BigDecimal("90.00"), new BigDecimal("100.00"));

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              Payment payment = paymentRepository.findAll().getFirst();
              Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();

              assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
              assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            });
  }
}
