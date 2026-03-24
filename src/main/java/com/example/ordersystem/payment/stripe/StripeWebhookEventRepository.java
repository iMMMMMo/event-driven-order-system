package com.example.ordersystem.payment.stripe;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StripeWebhookEventRepository extends JpaRepository<StripeWebhookEvent, String> {}
