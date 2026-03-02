package com.example.ordersystem.payment.event;

import com.example.ordersystem.shared.event.AbstractDomainEvent;

import java.util.UUID;

public class PaymentSucceededEvent extends AbstractDomainEvent {

    private final UUID orderId;

    public PaymentSucceededEvent(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }
}