package com.example.ordersystem.payment.stripe;

import com.example.ordersystem.payment.domain.Payment;
import com.example.ordersystem.payment.event.PaymentFailedEvent;
import com.example.ordersystem.payment.event.PaymentSucceededEvent;
import com.example.ordersystem.payment.repository.PaymentRepository;
import com.example.ordersystem.shared.event.DomainEventPublisher;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookService {

  private final StripeWebhookProperties properties;
  private final StripeWebhookEventRepository stripeWebhookEventRepository;
  private final PaymentRepository paymentRepository;
  private final DomainEventPublisher eventPublisher;
  private final ObjectMapper objectMapper;

  @Transactional
  public void handle(String payload, String stripeSignatureHeader)
      throws SignatureVerificationException {

    if (!properties.isConfigured()) {
      throw new IllegalStateException(
          "Stripe webhook is not configured (missing app.stripe.webhook.secret)");
    }

    Event event = Webhook.constructEvent(payload, stripeSignatureHeader, properties.secret());

    if (stripeWebhookEventRepository.existsById(event.getId())) {
      return;
    }

    try {
      stripeWebhookEventRepository.save(StripeWebhookEvent.processed(event.getId()));
    } catch (DataIntegrityViolationException ex) {
      return;
    }

    switch (event.getType()) {
      case "checkout.session.completed" -> handleCheckoutSessionCompleted(event, payload);
      case "checkout.session.expired" ->
          handleCheckoutSessionFailed(event, payload, "Checkout expired");
      case "checkout.session.async_payment_failed" ->
          handleCheckoutSessionFailed(event, payload, "Async payment failed");
      default -> log.debug("Ignoring Stripe event type={}", event.getType());
    }
  }

  private void handleCheckoutSessionCompleted(Event event, String payload) {
    UUID orderId = extractOrderIdFromPayload(event, payload);
    if (orderId == null) {
      return;
    }

    String idempotencyKey = "stripe-" + event.getId();
    if (paymentRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
      return;
    }

    Payment payment = Payment.create(orderId, idempotencyKey);
    payment.markSuccess();
    paymentRepository.save(payment);

    eventPublisher.publish(new PaymentSucceededEvent(orderId));
  }

  private void handleCheckoutSessionFailed(Event event, String payload, String reason) {
    UUID orderId = extractOrderIdFromPayload(event, payload);
    if (orderId == null) {
      return;
    }

    String idempotencyKey = "stripe-" + event.getId();
    if (paymentRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
      return;
    }

    Payment payment = Payment.create(orderId, idempotencyKey);
    payment.markFailed();
    paymentRepository.save(payment);

    eventPublisher.publish(new PaymentFailedEvent(orderId, reason));
  }

  private UUID extractOrderIdFromPayload(Event event, String payload) {
    try {
      JsonNode root = objectMapper.readTree(payload);
      JsonNode orderIdNode = root.path("data").path("object").path("metadata").path("orderId");
      if (orderIdNode.isMissingNode() || orderIdNode.isNull() || orderIdNode.asText().isBlank()) {
        log.warn("Stripe event {} missing data.object.metadata.orderId", event.getId());
        return null;
      }
      return UUID.fromString(orderIdNode.asText());
    } catch (Exception ex) {
      log.warn("Stripe event {} has invalid payload/orderId: {}", event.getId(), ex.getMessage());
      return null;
    }
  }
}
