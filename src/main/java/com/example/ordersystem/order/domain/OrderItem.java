package com.example.ordersystem.order.domain;

import com.example.ordersystem.shared.domain.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Column(name = "product_id", nullable = false)
  private UUID productId;

  @Column(nullable = false)
  private int quantity;

  @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
  private BigDecimal unitPrice;

  @Column(name = "line_total", nullable = false, precision = 15, scale = 2)
  private BigDecimal lineTotal;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Version private Long version;

  private OrderItem(
      Order order, UUID productId, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
    this.order = order;
    this.productId = productId;
    this.quantity = quantity;
    this.unitPrice = unitPrice;
    this.lineTotal = lineTotal;
    this.createdAt = Instant.now();
  }

  static OrderItem create(Order order, UUID productId, int quantity, BigDecimal unitPrice) {
    BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    return new OrderItem(order, productId, quantity, unitPrice, lineTotal);
  }
}
