package com.example.ordersystem.user.controller.dto;

public record CreateUserRequest(
        String email,
        String password
) {}
