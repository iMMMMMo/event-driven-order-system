package com.example.ordersystem.notification.event;

import com.example.ordersystem.inventory.event.InventoryReservedEvent;
import com.example.ordersystem.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private final NotificationService notificationService;

  @EventListener
  public void handleInventoryReserved(InventoryReservedEvent event) {
    notificationService.sendOrderCompletedNotification(event.getOrderId());
  }
}
