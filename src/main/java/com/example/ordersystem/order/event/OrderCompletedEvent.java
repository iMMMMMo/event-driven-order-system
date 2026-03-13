package com.example.ordersystem.order.event;

import com.example.ordersystem.shared.event.AbstractDomainEvent;
import java.util.UUID;

public class OrderCompletedEvent extends AbstractDomainEvent {

  private final UUID orderId;

  public OrderCompletedEvent(UUID orderId) {
    this.orderId = orderId;
  }

  public UUID getOrderId() {
    return orderId;
  }
}
