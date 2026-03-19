package com.example.ordersystem.payment.event;

import com.example.ordersystem.shared.event.AbstractDomainEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class PaymentFailedEvent extends AbstractDomainEvent {

  private final UUID orderId;
  private final String reason;

  @JsonCreator
  public PaymentFailedEvent(
      @JsonProperty("orderId") UUID orderId, @JsonProperty("reason") String reason) {
    this.orderId = orderId;
    this.reason = reason;
  }

  public UUID getOrderId() {
    return orderId;
  }

  public String getReason() {
    return reason;
  }
}
