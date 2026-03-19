package com.example.ordersystem.order.event;

import com.example.ordersystem.inventory.event.InventoryReservedEvent;
import com.example.ordersystem.order.repository.OrderRepository;
import com.example.ordersystem.order.service.OrderService;
import com.example.ordersystem.payment.event.PaymentFailedEvent;
import com.example.ordersystem.payment.event.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(
    topics = {"payment-events", "inventory-events", "order-events"},
    groupId = "order-group")
public class OrderEventListener {

  private final OrderService orderService;
  private final OrderRepository orderRepository;

  @KafkaHandler
  public void handlePaymentSucceeded(PaymentSucceededEvent event) {
    log.info("Payment success event received from Kafka for order: {}.", event.getOrderId());
    orderService.markAsPaid(event.getOrderId());
  }

  @KafkaHandler
  public void handlePaymentFailed(PaymentFailedEvent event) {
    log.warn(
        "Payment FAILED event received from Kafka for order: {}. Reason: {}. Cancelling order.",
        event.getOrderId(),
        event.getReason());
    orderService.cancelOrder(event.getOrderId());
  }

  @KafkaHandler
  public void handleInventoryReserved(InventoryReservedEvent event) {
    if (orderRepository.existsById(event.getOrderId())) {
      log.info(
          "Inventory reserved from Kafka for order: {}. Completing order.", event.getOrderId());
      orderService.completeOrder(event.getOrderId());
    } else {
      log.warn(
          "Skipping order completion for missing orderId {} after inventory reservation.",
          event.getOrderId());
    }
  }

  @KafkaHandler(isDefault = true)
  public void ignoreOthers(Object event) {
    if (event instanceof org.springframework.kafka.support.serializer.DeserializationException de) {
      log.error("Failed to deserialize message: {}", new String(de.getData()), de);
    } else {
      log.debug("Ignored event of type {}", event.getClass());
    }
  }
}
