package com.example.ordersystem.payment.event;

import com.example.ordersystem.order.event.OrderPaymentRequestedEvent;
import com.example.ordersystem.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

  private final PaymentService paymentService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handlePaymentRequested(OrderPaymentRequestedEvent event) {

    paymentService.processPayment(event.getOrderId(), event.getAmount(), event.getExpectedAmount());
  }
}
