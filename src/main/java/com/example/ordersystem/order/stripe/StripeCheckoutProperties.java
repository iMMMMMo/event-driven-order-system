package com.example.ordersystem.order.stripe;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.stripe")
public record StripeCheckoutProperties(
    String secretKey, String currency, String successUrl, String cancelUrl) {

  public boolean isConfigured() {
    return secretKey != null && !secretKey.isBlank();
  }
}
