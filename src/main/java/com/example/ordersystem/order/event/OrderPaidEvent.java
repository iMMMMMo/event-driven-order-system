package com.example.ordersystem.order.event;

import com.example.ordersystem.shared.event.AbstractDomainEvent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class OrderPaidEvent extends AbstractDomainEvent {

  private final UUID orderId;
  private final List<Line> items;

  public record Line(UUID productId, int quantity) {}

  @JsonCreator
  public OrderPaidEvent(
      @JsonProperty("orderId") UUID orderId, @JsonProperty("items") List<Line> items) {
    this.orderId = orderId;
    this.items = List.copyOf(Objects.requireNonNullElse(items, List.of()));
  }

  public OrderPaidEvent(UUID orderId) {
    this(orderId, List.of());
  }

  public UUID getOrderId() {
    return orderId;
  }

  public List<Line> getItems() {
    return items;
  }
}
