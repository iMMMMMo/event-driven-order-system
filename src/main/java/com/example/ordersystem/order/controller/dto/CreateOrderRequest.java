package com.example.ordersystem.order.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateOrderRequest(@NotNull @Positive BigDecimal totalAmount) {}
