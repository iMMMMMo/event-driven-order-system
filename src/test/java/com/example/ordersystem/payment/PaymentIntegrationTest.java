package com.example.ordersystem.payment;

import com.example.ordersystem.config.AbstractIntegrationTest;
import com.example.ordersystem.order.domain.Order;
import com.example.ordersystem.order.domain.OrderStatus;
import com.example.ordersystem.order.repository.OrderRepository;
import com.example.ordersystem.payment.domain.Payment;
import com.example.ordersystem.payment.domain.PaymentStatus;
import com.example.ordersystem.payment.repository.PaymentRepository;
import com.example.ordersystem.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void resetState() {
        paymentRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    void shouldPersistSuccessfulPaymentAndMarkOrderAsPaid() {
        Order order = orderRepository.save(Order.create(UUID.randomUUID(), "payment-success@example.com", new BigDecimal("100.00")));

        paymentService.processPayment(order.getId(), new BigDecimal("100.00"), new BigDecimal("100.00"));

        Payment payment = paymentRepository.findAll().getFirst();
        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void shouldPersistFailedPaymentAndCancelOrderWhenAmountDoesNotMatch() {
        Order order = orderRepository.save(Order.create(UUID.randomUUID(), "payment-failure@example.com", new BigDecimal("100.00")));

        paymentService.processPayment(order.getId(), new BigDecimal("90.00"), new BigDecimal("100.00"));

        Payment payment = paymentRepository.findAll().getFirst();
        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }
}