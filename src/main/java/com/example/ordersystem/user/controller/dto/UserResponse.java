package com.example.ordersystem.user.controller.dto;

import com.example.ordersystem.user.domain.UserRole;

public record UserResponse(String email, UserRole role) {}
