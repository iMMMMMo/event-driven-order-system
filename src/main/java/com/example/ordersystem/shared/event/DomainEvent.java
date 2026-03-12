package com.example.ordersystem.shared.event;

import java.time.Instant;

public interface DomainEvent {

  Instant occurredAt();
}
