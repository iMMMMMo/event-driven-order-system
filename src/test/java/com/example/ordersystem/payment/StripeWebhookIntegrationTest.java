package com.example.ordersystem.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.ordersystem.config.AbstractIntegrationTest;
import com.example.ordersystem.inventory.domain.InventoryItem;
import com.example.ordersystem.inventory.repository.InventoryRepository;
import com.example.ordersystem.order.domain.Order;
import com.example.ordersystem.order.domain.OrderStatus;
import com.example.ordersystem.order.repository.OrderRepository;
import com.example.ordersystem.payment.domain.Payment;
import com.example.ordersystem.payment.domain.PaymentStatus;
import com.example.ordersystem.payment.repository.PaymentRepository;
import com.example.ordersystem.payment.stripe.StripeWebhookEventRepository;
import com.example.ordersystem.user.domain.User;
import com.example.ordersystem.user.domain.UserRole;
import com.example.ordersystem.user.repository.UserRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "app.stripe.webhook.secret=whsec_test")
public class StripeWebhookIntegrationTest extends AbstractIntegrationTest {

  private static final String WEBHOOK_SECRET = "whsec_test";
  private static final BigDecimal DEFAULT_PRODUCT_PRICE = new BigDecimal("100.00");

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private PaymentRepository paymentRepository;

  @Autowired private OrderRepository orderRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private InventoryRepository inventoryRepository;

  @Autowired private StripeWebhookEventRepository stripeWebhookEventRepository;

  private User testUser;

  @BeforeEach
  void resetState() {
    stripeWebhookEventRepository.deleteAll();
    paymentRepository.deleteAll();
    orderRepository.deleteAll();
    userRepository.deleteAll();
    inventoryRepository.deleteAll();

    testUser =
        userRepository.save(
            User.builder()
                .email("stripe-webhook-test-" + UUID.randomUUID() + "@example.com")
                .password("password")
                .role(UserRole.USER)
                .build());
  }

  @Test
  void shouldMarkOrderAsCompletedWhenStripeCheckoutSessionCompletedArrives() {
    InventoryItem product = givenProductWithStock(10);
    Order savedOrder = givenOrderWithSingleItem("stripe-webhook-success@example.com", product);

    String payload = stripeCheckoutCompletedEventPayload(savedOrder.getId());
    String signature = stripeSignatureHeader(payload, WEBHOOK_SECRET, Instant.now());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add("Stripe-Signature", signature);

    ResponseEntity<String> webhookResponse =
        restTemplate.exchange(
            "/api/stripe/webhook",
            HttpMethod.POST,
            new HttpEntity<>(payload, headers),
            String.class);

    assertThat(webhookResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(
            () -> {
              Payment payment = singlePaymentForOrder(savedOrder.getId());
              Order updatedOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();
              InventoryItem updatedProduct =
                  inventoryRepository.findById(product.getId()).orElseThrow();

              assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
              assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
              assertThat(updatedProduct.getReserved()).isEqualTo(1);
            });
  }

  @Test
  void shouldCancelOrderWhenStripeCheckoutSessionExpiredArrives() {
    InventoryItem product = givenProductWithStock(10);
    Order savedOrder = givenOrderWithSingleItem("stripe-webhook-expired@example.com", product);

    String payload = stripeCheckoutEventPayload(savedOrder.getId(), "checkout.session.expired");
    String signature = stripeSignatureHeader(payload, WEBHOOK_SECRET, Instant.now());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add("Stripe-Signature", signature);

    ResponseEntity<String> webhookResponse =
        restTemplate.exchange(
            "/api/stripe/webhook",
            HttpMethod.POST,
            new HttpEntity<>(payload, headers),
            String.class);

    assertThat(webhookResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(
            () -> {
              Payment payment = singlePaymentForOrder(savedOrder.getId());
              Order updatedOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();
              InventoryItem updatedProduct =
                  inventoryRepository.findById(product.getId()).orElseThrow();

              assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
              assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
              assertThat(updatedProduct.getReserved()).isEqualTo(0);
            });
  }

  @Test
  void shouldCancelOrderWhenStripeCheckoutSessionAsyncPaymentFailedArrives() {
    InventoryItem product = givenProductWithStock(10);
    Order savedOrder = givenOrderWithSingleItem("stripe-webhook-async-failed@example.com", product);

    String payload =
        stripeCheckoutEventPayload(savedOrder.getId(), "checkout.session.async_payment_failed");
    String signature = stripeSignatureHeader(payload, WEBHOOK_SECRET, Instant.now());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add("Stripe-Signature", signature);

    ResponseEntity<String> webhookResponse =
        restTemplate.exchange(
            "/api/stripe/webhook",
            HttpMethod.POST,
            new HttpEntity<>(payload, headers),
            String.class);

    assertThat(webhookResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(
            () -> {
              Payment payment = singlePaymentForOrder(savedOrder.getId());
              Order updatedOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();
              InventoryItem updatedProduct =
                  inventoryRepository.findById(product.getId()).orElseThrow();

              assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
              assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
              assertThat(updatedProduct.getReserved()).isEqualTo(0);
            });
  }

  @Test
  void shouldBeIdempotentWhenSameStripeEventArrivesTwice() {
    InventoryItem product = givenProductWithStock(10);
    Order savedOrder = givenOrderWithSingleItem("stripe-webhook-idempotency@example.com", product);

    String eventId = "evt_test_idempotent_" + UUID.randomUUID();
    String payload =
        stripeCheckoutEventPayload(savedOrder.getId(), "checkout.session.completed", eventId);

    ResponseEntity<String> firstResponse =
        postWebhook(payload, stripeSignatureHeader(payload, WEBHOOK_SECRET, Instant.now()));

    assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(
            () -> {
              Payment payment = singlePaymentForOrder(savedOrder.getId());
              Order updatedOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();
              InventoryItem updatedProduct =
                  inventoryRepository.findById(product.getId()).orElseThrow();

              assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
              assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
              assertThat(updatedProduct.getReserved()).isEqualTo(1);
              assertThat(stripeWebhookEventRepository.existsById(eventId)).isTrue();
            });

    ResponseEntity<String> secondResponse =
        postWebhook(payload, stripeSignatureHeader(payload, WEBHOOK_SECRET, Instant.now()));
    assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              assertThat(paymentsForOrder(savedOrder.getId())).hasSize(1);
              assertThat(stripeWebhookEventRepository.findAll()).hasSize(1);

              Order updatedOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();
              InventoryItem updatedProduct =
                  inventoryRepository.findById(product.getId()).orElseThrow();

              assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
              assertThat(updatedProduct.getReserved()).isEqualTo(1);
            });
  }

  @Test
  void shouldReturn4xxAndNotModifyStateWhenSignatureIsInvalid() {
    InventoryItem product = givenProductWithStock(10);
    Order savedOrder =
        givenOrderWithSingleItem("stripe-webhook-invalid-signature@example.com", product);

    String payload = stripeCheckoutCompletedEventPayload(savedOrder.getId());
    String invalidSignature = stripeSignatureHeader(payload, "whsec_wrong", Instant.now());

    ResponseEntity<String> response = postWebhook(payload, invalidSignature);
    assertThat(response.getStatusCode().is4xxClientError()).isTrue();

    Order persistedOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();
    InventoryItem persistedProduct = inventoryRepository.findById(product.getId()).orElseThrow();

    assertThat(persistedOrder.getStatus()).isEqualTo(OrderStatus.CREATED);
    assertThat(persistedProduct.getReserved()).isEqualTo(0);
    assertThat(paymentsForOrder(savedOrder.getId())).isEmpty();
    assertThat(stripeWebhookEventRepository.findAll()).isEmpty();
  }

  @Test
  void shouldReturn4xxWhenSignatureHeaderIsMissingAndNotModifyState() {
    InventoryItem product = givenProductWithStock(10);
    Order savedOrder =
        givenOrderWithSingleItem("stripe-webhook-missing-signature@example.com", product);

    String payload = stripeCheckoutCompletedEventPayload(savedOrder.getId());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/stripe/webhook",
            HttpMethod.POST,
            new HttpEntity<>(payload, headers),
            String.class);

    assertThat(response.getStatusCode().is4xxClientError())
        .as(
            "Expected 4xx when Stripe-Signature header is missing, got %s with body: %s",
            response.getStatusCode(), response.getBody())
        .isTrue();

    Order persistedOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();
    InventoryItem persistedProduct = inventoryRepository.findById(product.getId()).orElseThrow();

    assertThat(persistedOrder.getStatus()).isEqualTo(OrderStatus.CREATED);
    assertThat(persistedProduct.getReserved()).isEqualTo(0);
    assertThat(paymentsForOrder(savedOrder.getId())).isEmpty();
    assertThat(stripeWebhookEventRepository.findAll()).isEmpty();
  }

  @Test
  void shouldReturn200AndNotModifyPaymentOrderInventoryForUnknownEventType() {
    InventoryItem product = givenProductWithStock(10);
    Order savedOrder =
        givenOrderWithSingleItem("stripe-webhook-unknown-event@example.com", product);

    String eventId = "evt_test_unknown_" + UUID.randomUUID();
    String payload = stripeCheckoutEventPayload(savedOrder.getId(), "unknown.event", eventId);

    ResponseEntity<String> response =
        postWebhook(payload, stripeSignatureHeader(payload, WEBHOOK_SECRET, Instant.now()));
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    await()
        .atMost(Duration.ofSeconds(2))
        .untilAsserted(
            () -> {
              Order persistedOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();
              InventoryItem persistedProduct =
                  inventoryRepository.findById(product.getId()).orElseThrow();

              assertThat(persistedOrder.getStatus()).isEqualTo(OrderStatus.CREATED);
              assertThat(persistedProduct.getReserved()).isEqualTo(0);
              assertThat(paymentsForOrder(savedOrder.getId())).isEmpty();
            });
  }

  @Test
  void shouldPersistWebhookEventOnceAndNotDuplicateOnRetryForUnknownEventType() {
    Order order =
        Order.create(
            testUser.getId(),
            "stripe-webhook-unknown-event-idempotency@example.com",
            new BigDecimal("100.00"));
    Order savedOrder = orderRepository.save(order);

    String eventId = "evt_test_unknown_idempotent_" + UUID.randomUUID();
    String payload = stripeCheckoutEventPayload(savedOrder.getId(), "unknown.event", eventId);

    ResponseEntity<String> firstResponse =
        postWebhook(payload, stripeSignatureHeader(payload, WEBHOOK_SECRET, Instant.now()));
    ResponseEntity<String> secondResponse =
        postWebhook(payload, stripeSignatureHeader(payload, WEBHOOK_SECRET, Instant.now()));

    assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              assertThat(stripeWebhookEventRepository.existsById(eventId)).isTrue();
              assertThat(stripeWebhookEventRepository.findAll()).hasSize(1);
              assertThat(paymentsForOrder(savedOrder.getId())).isEmpty();
            });
  }

  private static String stripeCheckoutCompletedEventPayload(UUID orderId) {
    return stripeCheckoutEventPayload(orderId, "checkout.session.completed");
  }

  private static String stripeCheckoutEventPayload(UUID orderId, String eventType) {
    return stripeCheckoutEventPayload(orderId, eventType, "evt_test_" + UUID.randomUUID());
  }

  private static String stripeCheckoutEventPayload(UUID orderId, String eventType, String eventId) {
    return "{"
        + "\"id\":\""
        + eventId
        + "\","
        + "\"object\":\"event\","
        + "\"api_version\":\"2020-08-27\","
        + "\"created\":1700000000,"
        + "\"data\":{"
        + "\"object\":{"
        + "\"id\":\"cs_test_"
        + UUID.randomUUID()
        + "\","
        + "\"object\":\"checkout.session\","
        + "\"metadata\":{\"orderId\":\""
        + orderId
        + "\"}"
        + "}"
        + "},"
        + "\"livemode\":false,"
        + "\"pending_webhooks\":1,"
        + "\"type\":\""
        + eventType
        + "\""
        + "}";
  }

  private ResponseEntity<String> postWebhook(String payload, String signatureHeader) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add("Stripe-Signature", signatureHeader);

    return restTemplate.exchange(
        "/api/stripe/webhook", HttpMethod.POST, new HttpEntity<>(payload, headers), String.class);
  }

  private InventoryItem givenProductWithStock(int available) {
    return inventoryRepository.save(
        InventoryItem.create(
            "STRIPE_WEBHOOK_PRODUCT_" + UUID.randomUUID(),
            DEFAULT_PRODUCT_PRICE,
            "Test",
            "General",
            available));
  }

  private Order givenOrderWithSingleItem(String email, InventoryItem product) {
    Order order = Order.create(testUser.getId(), email, DEFAULT_PRODUCT_PRICE);
    order.addItem(product.getId(), 1, product.getPrice());
    return orderRepository.save(order);
  }

  private List<Payment> paymentsForOrder(UUID orderId) {
    return paymentRepository.findAll().stream()
        .filter(p -> p.getOrderId().equals(orderId))
        .toList();
  }

  private Payment singlePaymentForOrder(UUID orderId) {
    List<Payment> payments = paymentsForOrder(orderId);
    assertThat(payments).hasSize(1);
    return payments.getFirst();
  }

  private static String stripeSignatureHeader(String payload, String secret, Instant now) {
    long timestamp = now.getEpochSecond();
    String signedPayload = timestamp + "." + payload;

    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] signatureBytes = mac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));
      String signature = HexFormat.of().formatHex(signatureBytes);
      return "t=" + timestamp + ",v1=" + signature;
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to create Stripe signature", ex);
    }
  }
}
