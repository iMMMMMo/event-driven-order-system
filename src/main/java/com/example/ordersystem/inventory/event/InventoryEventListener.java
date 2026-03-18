package com.example.ordersystem.inventory.event;

import com.example.ordersystem.inventory.service.InventoryService;
import com.example.ordersystem.order.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class InventoryEventListener {

  private final InventoryService inventoryService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderPaid(OrderPaidEvent event) {
    inventoryService.reserveForOrder(event.getOrderId());
  }
}
