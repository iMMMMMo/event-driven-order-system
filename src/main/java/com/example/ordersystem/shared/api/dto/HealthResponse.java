package com.example.ordersystem.shared.api.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"status", "timestamp"})
public record HealthResponse(String status, String timestamp) {}
