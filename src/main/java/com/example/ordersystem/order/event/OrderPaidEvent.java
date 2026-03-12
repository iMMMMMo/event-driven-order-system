package com.example.ordersystem.order.event;

import com.example.ordersystem.shared.event.AbstractDomainEvent;
import java.util.UUID;

public class OrderPaidEvent extends AbstractDomainEvent {

  private final UUID orderId;

  public OrderPaidEvent(UUID orderId) {
    this.orderId = orderId;
  }

  public UUID getOrderId() {
    return orderId;
  }
}
