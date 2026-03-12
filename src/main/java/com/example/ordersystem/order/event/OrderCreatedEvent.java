package com.example.ordersystem.order.event;

import com.example.ordersystem.shared.event.AbstractDomainEvent;
import java.math.BigDecimal;
import java.util.UUID;

public class OrderCreatedEvent extends AbstractDomainEvent {

  private final UUID orderId;
  private final String customerEmail;
  private final BigDecimal totalAmount;

  public OrderCreatedEvent(UUID orderId, String customerEmail, BigDecimal totalAmount) {
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
