package com.example.ordersystem.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.ordersystem.config.AbstractIntegrationTest;
import com.example.ordersystem.order.controller.dto.CreateOrderRequest;
import com.example.ordersystem.order.controller.dto.OrderResponse;
import com.example.ordersystem.order.domain.OrderStatus;
import com.example.ordersystem.order.repository.OrderRepository;
import com.example.ordersystem.user.controller.dto.AuthResponse;
import com.example.ordersystem.user.controller.dto.CreateUserRequest;
import com.example.ordersystem.user.controller.dto.LoginUserRequest;
import com.example.ordersystem.user.controller.dto.UserResponse;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

public class OrderIntegrationTest extends AbstractIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  @MockitoSpyBean private OrderRepository orderRepository;

  @Test
  void shouldCreateOrderForAuthenticatedUser() {
    AuthSession session = registerAndLogin();

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(session.token());

    ResponseEntity<OrderResponse> response =
        restTemplate.exchange(
            "/api/orders",
            HttpMethod.POST,
            new HttpEntity<>(new CreateOrderRequest(new BigDecimal("100.00")), headers),
            OrderResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getHeaders().getLocation()).isNotNull();
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().customerEmail()).isEqualTo(session.email());
    assertThat(response.getBody().status()).isNotNull();
  }

  @Test
  void shouldReturnNotFoundWhenOrderDoesNotExist() {
    AuthSession session = registerAndLogin();

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(session.token());

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/orders/" + UUID.randomUUID(),
            HttpMethod.GET,
            new HttpEntity<Void>(headers),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).contains("Order not found");
  }

  @Test
  void shouldReturnConflictWhenTryingToPayOrderThatIsNotCreated() {
    AuthSession session = registerAndLogin();

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(session.token());

    ResponseEntity<OrderResponse> createResponse =
        restTemplate.exchange(
            "/api/orders",
            HttpMethod.POST,
            new HttpEntity<>(new CreateOrderRequest(new BigDecimal("100.00")), headers),
            OrderResponse.class);

    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(createResponse.getBody()).isNotNull();

    UUID orderId = createResponse.getBody().id();

    ResponseEntity<OrderResponse> firstPayResponse =
        restTemplate.exchange(
            "/api/orders/" + orderId + "/pay?amount=100.00",
            HttpMethod.PATCH,
            new HttpEntity<Void>(headers),
            OrderResponse.class);

    assertThat(firstPayResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    await()
        .atMost(Duration.ofSeconds(5))
        .until(
            () ->
                orderRepository.findById(orderId).orElseThrow().getStatus()
                    == OrderStatus.COMPLETED);

    ResponseEntity<String> secondPayResponse =
        restTemplate.exchange(
            "/api/orders/" + orderId + "/pay?amount=100.00",
            HttpMethod.PATCH,
            new HttpEntity<Void>(headers),
            String.class);

    assertThat(secondPayResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(secondPayResponse.getBody()).contains("Only CREATED orders can be paid");
  }

  @Test
  void shouldReadOrderFromRedisCacheOnSecondRequest() {
    AuthSession session = registerAndLogin();

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(session.token());

    ResponseEntity<OrderResponse> createResponse =
        restTemplate.exchange(
            "/api/orders",
            HttpMethod.POST,
            new HttpEntity<>(new CreateOrderRequest(new BigDecimal("100.00")), headers),
            OrderResponse.class);

    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(createResponse.getBody()).isNotNull();

    UUID orderId = createResponse.getBody().id();
    clearInvocations(orderRepository);

    ResponseEntity<OrderResponse> firstGet =
        restTemplate.exchange(
            "/api/orders/" + orderId,
            HttpMethod.GET,
            new HttpEntity<Void>(headers),
            OrderResponse.class);

    ResponseEntity<OrderResponse> secondGet =
        restTemplate.exchange(
            "/api/orders/" + orderId,
            HttpMethod.GET,
            new HttpEntity<Void>(headers),
            OrderResponse.class);

    assertThat(firstGet.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(secondGet.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(firstGet.getBody()).isNotNull();
    assertThat(secondGet.getBody()).isNotNull();
    assertThat(secondGet.getBody().id()).isEqualTo(orderId);
    assertThat(secondGet.getBody()).isEqualTo(firstGet.getBody());

    verify(orderRepository, times(1)).findById(orderId);
  }

  private AuthSession registerAndLogin() {
    String email = "order-user-" + UUID.randomUUID() + "@example.com";
    String password = "Password123!";

    ResponseEntity<UserResponse> registerResponse =
        restTemplate.postForEntity(
            "/api/auth/register", new CreateUserRequest(email, password), UserResponse.class);

    assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<AuthResponse> loginResponse =
        restTemplate.postForEntity(
            "/api/auth/login", new LoginUserRequest(email, password), AuthResponse.class);

    assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(loginResponse.getBody()).isNotNull();
    assertThat(loginResponse.getBody().token()).isNotBlank();

    return new AuthSession(email, loginResponse.getBody().token());
  }

  private record AuthSession(String email, String token) {}
}
