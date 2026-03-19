package com.example.ordersystem.inventory.event;

import com.example.ordersystem.shared.event.AbstractDomainEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class InventoryReservedEvent extends AbstractDomainEvent {

  private final UUID orderId;

  @JsonCreator
  public InventoryReservedEvent(@JsonProperty("orderId") UUID orderId) {
    this.orderId = orderId;
  }

  public UUID getOrderId() {
    return orderId;
  }
}
