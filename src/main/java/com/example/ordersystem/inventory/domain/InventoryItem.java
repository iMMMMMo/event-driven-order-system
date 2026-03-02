package com.example.ordersystem.inventory.domain;

import com.example.ordersystem.shared.domain.BaseEntity;
import jakarta.persistence.*;
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

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int reserved;

    @Version
    private Long version;

    private InventoryItem(String productName, int quantity) {
        this.productName = productName;
        this.quantity = quantity;
        this.reserved = 0;
    }

    public static InventoryItem create(String productName, int quantity) {
        return new InventoryItem(productName, quantity);
    }

    public void reserve(int amount) {
        if (available() < amount) {
            throw new IllegalStateException("Not enough stock");
        }
        this.reserved += amount;
    }

    public int available() {
        return quantity - reserved;
    }
}