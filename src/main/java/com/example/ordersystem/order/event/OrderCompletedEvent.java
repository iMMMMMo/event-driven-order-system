package com.example.ordersystem.order.event;

import com.example.ordersystem.shared.event.AbstractDomainEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class OrderCompletedEvent extends AbstractDomainEvent {

  private final UUID orderId;

  @JsonCreator
  public OrderCompletedEvent(@JsonProperty("orderId") UUID orderId) {
    this.orderId = orderId;
  }

  public UUID getOrderId() {
    return orderId;
  }
}
