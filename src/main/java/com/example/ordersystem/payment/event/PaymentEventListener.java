package com.example.ordersystem.payment.event;

import com.example.ordersystem.order.event.OrderPaymentRequestedEvent;
import com.example.ordersystem.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@KafkaListener(topics = "order-events", groupId = "payment-group")
public class PaymentEventListener {

  private final PaymentService paymentService;

  @KafkaHandler
  public void handlePaymentRequested(OrderPaymentRequestedEvent event) {
    log.info("Received OrderPaymentRequestedEvent from Kafka for OrderId: {}", event.getOrderId());
    paymentService.processPayment(event.getOrderId(), event.getAmount(), event.getExpectedAmount());
  }

  @KafkaHandler(isDefault = true)
  public void ignoreOtherEvents(Object event) {
    if (event instanceof org.springframework.kafka.support.serializer.DeserializationException de) {
      log.error("Failed to deserialize message: {}", new String(de.getData()), de);
    } else {
      log.debug("Ignored event of type {}", event.getClass());
    }
  }
}
