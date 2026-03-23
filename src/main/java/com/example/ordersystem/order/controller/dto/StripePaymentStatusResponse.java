package com.example.ordersystem.order.controller.dto;

import java.util.UUID;

public record StripePaymentStatusResponse(UUID orderId, String status, String checkoutUrl) {}
