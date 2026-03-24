package com.example.ordersystem.order.event;

import com.example.ordersystem.order.stripe.StripeCheckoutRequestProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@KafkaListener(topics = "order-events", groupId = "stripe-checkout-group")
public class StripeCheckoutEventListener {

  private final StripeCheckoutRequestProcessor stripeCheckoutRequestProcessor;

  @KafkaHandler
  public void handleStripeCheckoutRequested(OrderStripeCheckoutRequestedEvent event) {
    log.info("Received OrderStripeCheckoutRequestedEvent for orderId {}", event.getOrderId());
    stripeCheckoutRequestProcessor.process(event.getOrderId(), event.getAmount());
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
