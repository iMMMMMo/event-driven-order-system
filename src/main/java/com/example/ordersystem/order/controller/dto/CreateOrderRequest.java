package com.example.ordersystem.order.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateOrderRequest(

        @Email
        @NotNull
        String customerEmail,

        @NotNull
        @Positive
        BigDecimal totalAmount
) {
}