package com.example.ordersystem.order.event;

import com.example.ordersystem.shared.event.AbstractDomainEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

public class OrderCreatedEvent extends AbstractDomainEvent {

  private final UUID orderId;
  private final String customerEmail;
  private final BigDecimal totalAmount;

  @JsonCreator
  public OrderCreatedEvent(
      @JsonProperty("orderId") UUID orderId,
      @JsonProperty("customerEmail") String customerEmail,
      @JsonProperty("totalAmount") BigDecimal totalAmount) {
    this.orderId = orderId;
    this.customerEmail = customerEmail;
    this.totalAmount = totalAmount;
  }

  public UUID getOrderId() {
    return orderId;
  }

  public String getCustomerEmail() {
    return customerEmail;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }
}
