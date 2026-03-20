package com.example.ordersystem.order.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
    UUID productId, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {}
