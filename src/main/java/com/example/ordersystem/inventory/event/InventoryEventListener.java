package com.example.ordersystem.inventory.event;

import com.example.ordersystem.inventory.service.InventoryService;
import com.example.ordersystem.order.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryEventListener {

  private final InventoryService inventoryService;

  @EventListener
  public void handleOrderPaid(OrderPaidEvent event) {
    inventoryService.reserveForOrder(event.getOrderId());
  }
}
