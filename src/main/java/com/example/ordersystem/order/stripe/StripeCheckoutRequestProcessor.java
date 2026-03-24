package com.example.ordersystem.order.stripe;

import com.example.ordersystem.order.domain.StripeCheckoutRequest;
import com.example.ordersystem.order.domain.StripeCheckoutRequestStatus;
import com.example.ordersystem.order.repository.StripeCheckoutRequestRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeCheckoutRequestProcessor {

  private final StripeCheckoutRequestRepository stripeCheckoutRequestRepository;
  private final StripeCheckoutSessionClient stripeCheckoutSessionClient;

  @Transactional
  public void process(UUID orderId, BigDecimal amount) {
    StripeCheckoutRequest request =
        stripeCheckoutRequestRepository.findByOrderId(orderId).orElse(null);

    if (request == null) {
      log.warn("Stripe checkout request missing for orderId {}", orderId);
      return;
    }

    if (request.getStatus() == StripeCheckoutRequestStatus.READY) {
      return;
    }

    if (request.getStatus() == StripeCheckoutRequestStatus.FAILED) {
      return;
    }

    try {
      String checkoutUrl = stripeCheckoutSessionClient.createCheckoutUrl(orderId, amount);
      request.markReady(checkoutUrl);
      stripeCheckoutRequestRepository.save(request);
      log.info("Stripe checkout READY for orderId {}", orderId);
    } catch (Exception e) {
      request.markFailed(e.getMessage());
      stripeCheckoutRequestRepository.save(request);
      log.warn("Stripe checkout FAILED for orderId {}: {}", orderId, e.getMessage());
    }
  }
}
