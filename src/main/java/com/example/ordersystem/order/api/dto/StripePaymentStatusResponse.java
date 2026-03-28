package com.example.ordersystem.order.api.dto;

import java.util.UUID;

public record StripePaymentStatusResponse(UUID orderId, String status, String checkoutUrl) {}
