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
    InventoryItem product =
        inventoryRepository.save(
            InventoryItem.create(
                "STRIPE_WEBHOOK_PRODUCT_" + UUID.randomUUID(),
                new BigDecimal("100.00"),
                "Test",
                "General",
                10));

    Order order =
        Order.create(
            testUser.getId(), "stripe-webhook-success@example.com", new BigDecimal("100.00"));
    order.addItem(product.getId(), 1, product.getPrice());
    Order savedOrder = orderRepository.save(order);

    String payload = stripeCheckoutCompletedEventPayload(savedOrder.getId());
    String signature = stripeSignatureHeader(payload, "whsec_test", Instant.now());

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
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              Payment payment = paymentRepository.findAll().getFirst();
              Order updatedOrder = orderRepository.findById(savedOrder.getId()).orElseThrow();
              InventoryItem updatedProduct =
                  inventoryRepository.findById(product.getId()).orElseThrow();

              assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
              assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
              assertThat(updatedProduct.getReserved()).isEqualTo(1);
            });
  }

  private static String stripeCheckoutCompletedEventPayload(UUID orderId) {
    return "{"
        + "\"id\":\"evt_test_"
        + UUID.randomUUID()
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
        + "\"type\":\"checkout.session.completed\""
        + "}";
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
