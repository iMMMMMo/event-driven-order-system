package com.example.ordersystem.shared.event;

import java.time.Instant;

public abstract class AbstractDomainEvent implements DomainEvent {

    private final Instant occurredAt = Instant.now();

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }
}