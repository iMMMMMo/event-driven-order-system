package com.example.ordersystem.user.api.dto;

import com.example.ordersystem.user.domain.UserRole;

public record UserResponse(String email, UserRole role) {}
