package com.example.ordersystem.notification.event;

import com.example.ordersystem.notification.service.NotificationService;
import com.example.ordersystem.order.event.OrderCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private final NotificationService notificationService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleOrderCompleted(OrderCompletedEvent event) {
    notificationService.sendOrderCompletedNotification(event.getOrderId());
  }
}
