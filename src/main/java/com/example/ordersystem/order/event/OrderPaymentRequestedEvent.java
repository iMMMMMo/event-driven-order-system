package com.example.ordersystem.order.event;

import com.example.ordersystem.shared.event.AbstractDomainEvent;
import java.math.BigDecimal;
import java.util.UUID;

public class OrderPaymentRequestedEvent extends AbstractDomainEvent {

    private final UUID orderId;
    private final BigDecimal amount;
    private final BigDecimal expectedAmount;

    public OrderPaymentRequestedEvent(UUID orderId,
                                      BigDecimal amount,
                                      BigDecimal expectedAmount) {
        this.orderId = orderId;
        this.amount = amount;
        this.expectedAmount = expectedAmount;
    }

    public UUID getOrderId() { return orderId; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getExpectedAmount() { return expectedAmount; }
}
