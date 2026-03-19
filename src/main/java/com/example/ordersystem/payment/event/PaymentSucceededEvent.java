package com.example.ordersystem.payment.event;

import com.example.ordersystem.shared.event.AbstractDomainEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class PaymentSucceededEvent extends AbstractDomainEvent {

  private final UUID orderId;

  @JsonCreator
  public PaymentSucceededEvent(@JsonProperty("orderId") UUID orderId) {
    this.orderId = orderId;
  }

  public UUID getOrderId() {
    return orderId;
  }
}
