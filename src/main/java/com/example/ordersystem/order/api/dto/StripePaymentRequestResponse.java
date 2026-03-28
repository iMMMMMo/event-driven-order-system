package com.example.ordersystem.order.api.dto;

import java.util.UUID;

public record StripePaymentRequestResponse(UUID orderId, String status, String pollUrl) {}
