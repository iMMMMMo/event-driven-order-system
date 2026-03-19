package com.example.ordersystem.notification.event;

import com.example.ordersystem.notification.service.NotificationService;
import com.example.ordersystem.order.event.OrderCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@KafkaListener(topics = "order-events", groupId = "notification-group")
public class NotificationEventListener {

  private final NotificationService notificationService;

  @KafkaHandler
  public void handleOrderCompleted(OrderCompletedEvent event) {
    log.info("Received OrderCompletedEvent from Kafka for OrderId: {}", event.getOrderId());
    notificationService.sendOrderCompletedNotification(event.getOrderId());
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
