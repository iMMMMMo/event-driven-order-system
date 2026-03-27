package com.example.ordersystem.inventory.event;

import com.example.ordersystem.inventory.service.InventoryService;
import com.example.ordersystem.order.event.OrderPaidEvent;
import java.util.List;
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
    List<InventoryService.ReservationLine> items =
        event.getItems().stream()
            .map(i -> new InventoryService.ReservationLine(i.productId(), i.quantity()))
            .toList();
    inventoryService.reserveForOrder(event.getOrderId(), items);
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
