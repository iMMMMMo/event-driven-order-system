package com.example.ordersystem.order.repository;

import com.example.ordersystem.order.domain.StripeCheckoutRequest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StripeCheckoutRequestRepository
    extends JpaRepository<StripeCheckoutRequest, UUID> {

  Optional<StripeCheckoutRequest> findByOrderId(UUID orderId);
}
