package com.example.ordersystem.order.event;

import com.example.ordersystem.shared.event.AbstractDomainEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

public class OrderPaymentRequestedEvent extends AbstractDomainEvent {

  private final UUID orderId;
  private final BigDecimal amount;
  private final BigDecimal expectedAmount;

  @JsonCreator
  public OrderPaymentRequestedEvent(
      @JsonProperty("orderId") UUID orderId,
      @JsonProperty("amount") BigDecimal amount,
      @JsonProperty("expectedAmount") BigDecimal expectedAmount) {
    this.orderId = orderId;
    this.amount = amount;
    this.expectedAmount = expectedAmount;
  }

  public UUID getOrderId() {
    return orderId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public BigDecimal getExpectedAmount() {
    return expectedAmount;
  }
}
