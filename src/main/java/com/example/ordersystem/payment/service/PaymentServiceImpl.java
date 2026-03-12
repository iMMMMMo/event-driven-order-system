package com.example.ordersystem.payment.service;

import com.example.ordersystem.payment.domain.Payment;
import com.example.ordersystem.payment.event.PaymentFailedEvent;
import com.example.ordersystem.payment.event.PaymentSucceededEvent;
import com.example.ordersystem.payment.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

  private final PaymentRepository paymentRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  public void processPayment(UUID orderId, BigDecimal amount, BigDecimal expectedAmount) {

    String idempotencyKey = "pay-" + orderId + "-" + amount;

    if (paymentRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
      return;
    }

    Payment payment = Payment.create(orderId, idempotencyKey);

    if (amount.compareTo(expectedAmount) == 0) {
      payment.markSuccess();
      paymentRepository.save(payment);
      eventPublisher.publishEvent(new PaymentSucceededEvent(orderId));
    } else {
      payment.markFailed();
      paymentRepository.save(payment);
      eventPublisher.publishEvent(new PaymentFailedEvent(orderId, "Incorrect amount"));
    }
  }
}
