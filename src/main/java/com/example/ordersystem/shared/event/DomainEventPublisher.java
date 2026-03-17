package com.example.ordersystem.shared.event;

public interface DomainEventPublisher {

  void publish(DomainEvent event);
}
