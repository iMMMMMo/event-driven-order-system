package com.example.ordersystem.order.domain;

import com.example.ordersystem.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stripe_checkout_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StripeCheckoutRequest extends BaseEntity {

  @Column(nullable = false, unique = true)
  private UUID orderId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StripeCheckoutRequestStatus status;

  @Column(columnDefinition = "TEXT")
  private String checkoutUrl;

  @Column(columnDefinition = "TEXT")
  private String failureReason;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @Version private Long version;

  private StripeCheckoutRequest(UUID orderId) {
    this.orderId = orderId;
    this.status = StripeCheckoutRequestStatus.REQUESTED;
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
  }

  public static StripeCheckoutRequest requested(UUID orderId) {
    return new StripeCheckoutRequest(orderId);
  }

  public void markReady(String checkoutUrl) {
    this.status = StripeCheckoutRequestStatus.READY;
    this.checkoutUrl = checkoutUrl;
    this.failureReason = null;
    this.updatedAt = Instant.now();
  }

  public void markFailed(String reason) {
    this.status = StripeCheckoutRequestStatus.FAILED;
    this.failureReason = reason;
    this.updatedAt = Instant.now();
  }
}
