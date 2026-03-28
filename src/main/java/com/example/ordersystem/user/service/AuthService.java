package com.example.ordersystem.user.service;

import com.example.ordersystem.user.api.dto.AuthResponse;
import com.example.ordersystem.user.api.dto.CreateUserRequest;
import com.example.ordersystem.user.api.dto.LoginUserRequest;
import com.example.ordersystem.user.api.dto.UserResponse;

public interface AuthService {

  UserResponse register(CreateUserRequest request);

  AuthResponse login(LoginUserRequest request);

  AuthResponse getCurrentToken();

  UserResponse getCurrentUser();
}
