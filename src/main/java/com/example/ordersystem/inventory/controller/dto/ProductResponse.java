package com.example.ordersystem.inventory.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
    UUID id,
    String productName,
    BigDecimal price,
    String description,
    String category,
    int availableQuantity) {}
