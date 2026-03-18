package com.example.ordersystem.shared.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "outbox_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

  @Id private UUID id;

  @Column(nullable = false)
  private String eventType;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Column(nullable = false)
  private Instant occurredAt;

  @Column() private Instant publishedAt;

  @Column(nullable = false)
  private Integer attempts;

  @Column(columnDefinition = "TEXT")
  private String lastError;

  public static OutboxEvent pending(String eventType, String payload, Instant occurredAt) {
    OutboxEvent outboxEvent = new OutboxEvent();
    outboxEvent.id = UUID.randomUUID();
    outboxEvent.eventType = eventType;
    outboxEvent.payload = payload;
    outboxEvent.occurredAt = occurredAt;
    outboxEvent.attempts = 0;
    return outboxEvent;
  }

  public void markPublished(Instant publishedAt) {
    this.publishedAt = publishedAt;
    this.lastError = null;
  }

  public void markFailed(String errorMessage) {
    this.attempts = this.attempts + 1;
    this.lastError = errorMessage;
  }
}
