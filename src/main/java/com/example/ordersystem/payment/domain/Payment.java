package com.example.ordersystem.payment.domain;

import com.example.ordersystem.shared.exception.ConflictException;
import com.example.ordersystem.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "payments",
        uniqueConstraints = @UniqueConstraint(columnNames = "idempotencyKey"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Column(nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false, updatable = false)
    private String idempotencyKey;

    @Version
    private Long version;

    private Payment(UUID orderId, String idempotencyKey) {
        this.orderId = orderId;
        this.idempotencyKey = idempotencyKey;
        this.status = PaymentStatus.PENDING;
    }

    public static Payment create(UUID orderId, String idempotencyKey) {
        return new Payment(orderId, idempotencyKey);
    }

    public void markSuccess() {
        if (status != PaymentStatus.PENDING) {
            throw new ConflictException("Payment already processed");
        }
        this.status = PaymentStatus.SUCCESS;
    }

    public void markFailed() {
        if (status != PaymentStatus.PENDING) {
            throw new ConflictException("Payment already processed");
        }
        this.status = PaymentStatus.FAILED;
    }
}