package com.example.ordersystem.order.event;

import com.example.ordersystem.shared.event.AbstractDomainEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

public class OrderStripeCheckoutRequestedEvent extends AbstractDomainEvent {

  private final UUID orderId;
  private final BigDecimal amount;

  @JsonCreator
  public OrderStripeCheckoutRequestedEvent(
      @JsonProperty("orderId") UUID orderId, @JsonProperty("amount") BigDecimal amount) {
    this.orderId = orderId;
    this.amount = amount;
  }

  public UUID getOrderId() {
    return orderId;
  }

  public BigDecimal getAmount() {
    return amount;
  }
}
