package com.example.ordersystem.payment.event;

import com.example.ordersystem.shared.event.AbstractDomainEvent;
import java.util.UUID;

public class PaymentFailedEvent extends AbstractDomainEvent {

  private final UUID orderId;
  private final String reason;

  public PaymentFailedEvent(UUID orderId, String reason) {
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
