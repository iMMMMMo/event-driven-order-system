package com.example.ordersystem.notification.event;

import com.example.ordersystem.notification.service.NotificationService;
import com.example.ordersystem.order.event.OrderCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private final NotificationService notificationService;

  @EventListener
  public void handleOrderCompleted(OrderCompletedEvent event) {
    notificationService.sendOrderCompletedNotification(event.getOrderId());
  }
}
