package com.example.ordersystem.payment.service;

import java.util.UUID;

public interface PaymentService {

    void processPayment(UUID orderId, String idempotencyKey);
}