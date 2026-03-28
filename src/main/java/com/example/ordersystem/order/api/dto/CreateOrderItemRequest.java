package com.example.ordersystem.order.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record CreateOrderItemRequest(@NotNull UUID productId, @Positive int quantity) {}
