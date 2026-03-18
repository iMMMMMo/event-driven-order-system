package com.example.ordersystem.shared.outbox;

import com.example.ordersystem.inventory.event.InventoryReservedEvent;
import com.example.ordersystem.order.event.OrderCancelledEvent;
import com.example.ordersystem.order.event.OrderCompletedEvent;
import com.example.ordersystem.order.event.OrderCreatedEvent;
import com.example.ordersystem.order.event.OrderPaidEvent;
import com.example.ordersystem.order.event.OrderPaymentRequestedEvent;
import com.example.ordersystem.payment.event.PaymentFailedEvent;
import com.example.ordersystem.payment.event.PaymentSucceededEvent;
import com.example.ordersystem.shared.event.DomainEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisherJob {

  private final OutboxEventRepository outboxEventRepository;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final ObjectMapper objectMapper;

  @Value("${app.outbox.publisher.batch-size:50}")
  private int batchSize;

  @Scheduled(fixedDelayString = "${app.outbox.publisher.fixed-delay-ms:500}")
  @Transactional
  public void publishPendingEvents() {
    List<OutboxEvent> pendingEvents =
        outboxEventRepository.findByPublishedAtIsNullOrderByOccurredAtAsc(
            PageRequest.of(0, batchSize));

    for (OutboxEvent outboxEvent : pendingEvents) {
      try {
        DomainEvent domainEvent = deserialize(outboxEvent);
        applicationEventPublisher.publishEvent(domainEvent);
        outboxEvent.markPublished(Instant.now());
      } catch (Exception ex) {
        log.error(
            "Failed to publish outbox event id={} type={}",
            outboxEvent.getId(),
            outboxEvent.getEventType(),
            ex);
        outboxEvent.markFailed(ex.getMessage());
      }
    }
  }

  private DomainEvent deserialize(OutboxEvent outboxEvent) throws IOException {
    JsonNode payload = objectMapper.readTree(outboxEvent.getPayload());

    return switch (outboxEvent.getEventType()) {
      case "com.example.ordersystem.order.event.OrderCreatedEvent" ->
          new OrderCreatedEvent(
              uuid(payload, "orderId"),
              text(payload, "customerEmail"),
              decimal(payload, "totalAmount"));
      case "com.example.ordersystem.order.event.OrderPaymentRequestedEvent" ->
          new OrderPaymentRequestedEvent(
              uuid(payload, "orderId"),
              decimal(payload, "amount"),
              decimal(payload, "expectedAmount"));
      case "com.example.ordersystem.order.event.OrderPaidEvent" ->
          new OrderPaidEvent(uuid(payload, "orderId"));
      case "com.example.ordersystem.order.event.OrderCompletedEvent" ->
          new OrderCompletedEvent(uuid(payload, "orderId"));
      case "com.example.ordersystem.order.event.OrderCancelledEvent" ->
          new OrderCancelledEvent(uuid(payload, "orderId"));
      case "com.example.ordersystem.payment.event.PaymentSucceededEvent" ->
          new PaymentSucceededEvent(uuid(payload, "orderId"));
      case "com.example.ordersystem.payment.event.PaymentFailedEvent" ->
          new PaymentFailedEvent(uuid(payload, "orderId"), text(payload, "reason"));
      case "com.example.ordersystem.inventory.event.InventoryReservedEvent" ->
          new InventoryReservedEvent(uuid(payload, "orderId"));
      default ->
          throw new IllegalArgumentException(
              "Unsupported event type: " + outboxEvent.getEventType());
    };
  }

  private UUID uuid(JsonNode payload, String fieldName) {
    return UUID.fromString(text(payload, fieldName));
  }

  private BigDecimal decimal(JsonNode payload, String fieldName) {
    return new BigDecimal(text(payload, fieldName));
  }

  private String text(JsonNode payload, String fieldName) {
    JsonNode value = payload.get(fieldName);
    if (value == null || value.isNull()) {
      throw new IllegalArgumentException("Missing field in outbox payload: " + fieldName);
    }
    return value.asText();
  }
}
