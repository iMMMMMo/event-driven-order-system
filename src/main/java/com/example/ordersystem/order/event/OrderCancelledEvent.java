package com.example.ordersystem.order.event;

import com.example.ordersystem.shared.event.AbstractDomainEvent;
import java.util.UUID;

public class OrderCancelledEvent extends AbstractDomainEvent {

  private final UUID orderId;

  public OrderCancelledEvent(UUID orderId) {
    this.orderId = orderId;
  }

  public UUID getOrderId() {
    return orderId;
  }
}
