package com.example.ordersystem.order.stripe;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StripeCheckoutSessionClientImpl implements StripeCheckoutSessionClient {

  private final StripeCheckoutProperties properties;

  @Override
  public String createCheckoutUrl(UUID orderId, BigDecimal amount) {
    if (!properties.isConfigured()) {
      throw new IllegalStateException("Stripe is not configured (missing app.stripe.secret-key)");
    }

    if (amount == null || amount.signum() <= 0) {
      throw new IllegalArgumentException("Amount must be positive");
    }

    String currency = properties.currency() == null ? "pln" : properties.currency();
    long unitAmount = toMinorUnits(amount, currency);

    Stripe.apiKey = properties.secretKey();

    SessionCreateParams params =
        SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(properties.successUrl())
            .setCancelUrl(properties.cancelUrl())
            .putAllMetadata(Map.of("orderId", orderId.toString()))
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(currency)
                            .setUnitAmount(unitAmount)
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName("Order " + orderId)
                                    .build())
                            .build())
                    .build())
            .build();

    RequestOptions requestOptions =
        RequestOptions.builder().setIdempotencyKey("checkout-" + orderId).build();

    try {
      Session session = Session.create(params, requestOptions);
      if (session.getUrl() == null || session.getUrl().isBlank()) {
        throw new IllegalStateException("Stripe session created without URL");
      }
      return session.getUrl();
    } catch (StripeException e) {
      throw new IllegalStateException("Stripe API error: " + e.getMessage(), e);
    }
  }

  private long toMinorUnits(BigDecimal amount, String currency) {
    Currency javaCurrency = Currency.getInstance(currency.toUpperCase());
    int fractionDigits = javaCurrency.getDefaultFractionDigits();
    BigDecimal scaled = amount.movePointRight(fractionDigits);
    return scaled.setScale(0, RoundingMode.HALF_UP).longValueExact();
  }
}
