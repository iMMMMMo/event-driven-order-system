package com.example.ordersystem.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ordersystem.config.AbstractIntegrationTest;
import com.example.ordersystem.user.api.dto.AuthResponse;
import com.example.ordersystem.user.api.dto.CreateUserRequest;
import com.example.ordersystem.user.api.dto.LoginUserRequest;
import com.example.ordersystem.user.api.dto.UserResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class AuthSecurityIntegrationTest extends AbstractIntegrationTest {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void shouldRequireJwtForProfileEndpoint() {
    ResponseEntity<String> response =
        restTemplate.exchange("/api/auth/profile", HttpMethod.GET, null, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void shouldReturnProfileWhenJwtIsValid() {
    String email = "user-" + UUID.randomUUID() + "@example.com";
    String password = "Password123!";

    ResponseEntity<UserResponse> registerResponse =
        restTemplate.postForEntity(
            "/api/auth/register", new CreateUserRequest(email, password), UserResponse.class);

    assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(registerResponse.getBody()).isNotNull();
    assertThat(registerResponse.getBody().email()).isEqualTo(email);

    ResponseEntity<AuthResponse> loginResponse =
        restTemplate.postForEntity(
            "/api/auth/login", new LoginUserRequest(email, password), AuthResponse.class);

    assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(loginResponse.getBody()).isNotNull();
    assertThat(loginResponse.getBody().token()).isNotBlank();

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(loginResponse.getBody().token());

    ResponseEntity<UserResponse> profileResponse =
        restTemplate.exchange(
            "/api/auth/profile",
            HttpMethod.GET,
            new org.springframework.http.HttpEntity<Void>(headers),
            UserResponse.class);

    assertThat(profileResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(profileResponse.getBody()).isNotNull();
    assertThat(profileResponse.getBody().email()).isEqualTo(email);
    assertThat(profileResponse.getBody().role()).isNotNull();
  }
}
