package com.example.ordersystem.shared.event;

import com.example.ordersystem.shared.outbox.OutboxEvent;
import com.example.ordersystem.shared.outbox.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Transactional
public class SpringDomainEventPublisher implements DomainEventPublisher {

  private final OutboxEventRepository outboxEventRepository;
  private final ObjectMapper objectMapper;

  @Override
  public void publish(DomainEvent event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      OutboxEvent outboxEvent =
          OutboxEvent.pending(event.getClass().getName(), payload, event.occurredAt());
      outboxEventRepository.save(outboxEvent);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Failed to serialize domain event " + event.getClass(), ex);
    }
  }
}
