package com.example.ordersystem.user.controller.dto;

import com.example.ordersystem.user.domain.UserRole;
import java.util.UUID;

public record UserResponse(
        String email,
        UserRole role
) {}