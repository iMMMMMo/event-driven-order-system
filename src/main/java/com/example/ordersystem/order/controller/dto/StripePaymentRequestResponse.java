package com.example.ordersystem.order.controller.dto;

import java.util.UUID;

public record StripePaymentRequestResponse(UUID orderId, String status, String pollUrl) {}
