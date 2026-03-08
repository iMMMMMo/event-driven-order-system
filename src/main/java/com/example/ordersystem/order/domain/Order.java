package com.example.ordersystem.order.domain;

import com.example.ordersystem.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String customerEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    private Long version;

    private Order(UUID userId, String customerEmail, BigDecimal totalAmount) {
        this.userId = userId;
        this.customerEmail = customerEmail;
        this.totalAmount = totalAmount;
        this.status = OrderStatus.CREATED;
        this.createdAt = Instant.now();
    }

    public static Order create(UUID userId, String customerEmail, BigDecimal totalAmount) {
        return new Order(userId, customerEmail, totalAmount);
    }

    public void markAsPaid() {
        if (this.status != OrderStatus.CREATED) {
            throw new IllegalStateException("Only CREATED orders can be marked as PAID");
        }
        this.status = OrderStatus.PAID;
    }

    public void cancel() {
        if (this.status != OrderStatus.CREATED) {
            throw new IllegalStateException("Only CREATED orders can be marked as CANCELLED");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public void complete() {
        if (this.status != OrderStatus.PAID) {
            throw new IllegalStateException("Only PAID orders can be COMPLETED");
        }
        this.status = OrderStatus.COMPLETED;
    }
}