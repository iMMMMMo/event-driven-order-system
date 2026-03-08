package com.example.ordersystem.payment.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {

    void processPayment(UUID orderId, BigDecimal amount, BigDecimal expectedAmount);
}