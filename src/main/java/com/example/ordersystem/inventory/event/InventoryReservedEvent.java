package com.example.ordersystem.inventory.event;

import com.example.ordersystem.shared.event.AbstractDomainEvent;
import java.util.UUID;

public class InventoryReservedEvent extends AbstractDomainEvent {

  private final UUID orderId;

  public InventoryReservedEvent(UUID orderId) {
    this.orderId = orderId;
  }

  public UUID getOrderId() {
    return orderId;
  }
}
