package com.example.ordersystem.inventory.domain;

import com.example.ordersystem.shared.domain.BaseEntity;
import com.example.ordersystem.shared.exception.ConflictException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryItem extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String productName;

  @Column(nullable = false, precision = 15, scale = 2)
  private BigDecimal price;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column private String category;

  @Column(nullable = false)
  private int quantity;

  @Column(nullable = false)
  private int reserved;

  @Version private Long version;

  private InventoryItem(
      String productName, BigDecimal price, String description, String category, int quantity) {
    this.productName = productName;
    this.price = price;
    this.description = description;
    this.category = category;
    this.quantity = quantity;
    this.reserved = 0;
  }

  public static InventoryItem create(
      String productName, BigDecimal price, String description, String category, int quantity) {
    return new InventoryItem(productName, price, description, category, quantity);
  }

  public void reserve(int amount) {
    if (available() < amount) {
      throw new ConflictException("Not enough stock");
    }
    this.reserved += amount;
  }

  public int available() {
    return quantity - reserved;
  }
}
