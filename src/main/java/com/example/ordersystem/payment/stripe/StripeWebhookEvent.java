package com.example.ordersystem.payment.stripe;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stripe_webhook_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StripeWebhookEvent {

  @Id
  @Column(name = "event_id", nullable = false, updatable = false)
  private String eventId;

  @Column(name = "processed_at", nullable = false, updatable = false)
  private Instant processedAt;

  private StripeWebhookEvent(String eventId) {
    this.eventId = eventId;
    this.processedAt = Instant.now();
  }

  public static StripeWebhookEvent processed(String eventId) {
    return new StripeWebhookEvent(eventId);
  }
}
