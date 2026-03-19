package com.example.ordersystem.inventory.event;

import com.example.ordersystem.inventory.service.InventoryService;
import com.example.ordersystem.order.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@KafkaListener(topics = "order-events", groupId = "inventory-group")
public class InventoryEventListener {

  private final InventoryService inventoryService;

  @KafkaHandler
  public void handleOrderPaid(OrderPaidEvent event) {
    log.info("Received OrderPaidEvent from Kafka for OrderId: {}", event.getOrderId());
    inventoryService.reserveForOrder(event.getOrderId());
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
