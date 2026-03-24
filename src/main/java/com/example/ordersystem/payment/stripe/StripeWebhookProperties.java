package com.example.ordersystem.payment.stripe;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.stripe.webhook")
public record StripeWebhookProperties(String secret) {

  public boolean isConfigured() {
    return secret != null && !secret.isBlank();
  }
}
