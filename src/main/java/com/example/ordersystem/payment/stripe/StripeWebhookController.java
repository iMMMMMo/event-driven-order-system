package com.example.ordersystem.payment.stripe;

import com.stripe.exception.SignatureVerificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

  private final StripeWebhookService stripeWebhookService;

  @PostMapping("/webhook")
  public ResponseEntity<Void> handleWebhook(
      @RequestBody String payload, @RequestHeader("Stripe-Signature") String signatureHeader) {

    try {
      stripeWebhookService.handle(payload, signatureHeader);
      return ResponseEntity.ok().build();
    } catch (SignatureVerificationException ex) {
      log.warn("Invalid Stripe webhook signature: {}", ex.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }
}
