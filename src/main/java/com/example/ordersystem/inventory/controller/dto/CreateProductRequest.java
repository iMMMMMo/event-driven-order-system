package com.example.ordersystem.inventory.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateProductRequest(
    @NotBlank String productName,
    @NotNull @DecimalMin("0.0") BigDecimal price,
    String description,
    String category,
    @NotNull int quantity) {}
