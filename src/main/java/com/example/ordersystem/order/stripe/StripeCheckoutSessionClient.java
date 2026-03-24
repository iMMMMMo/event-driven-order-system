package com.example.ordersystem.order.stripe;

import java.math.BigDecimal;
import java.util.UUID;

public interface StripeCheckoutSessionClient {

  String createCheckoutUrl(UUID orderId, BigDecimal amount);
}
