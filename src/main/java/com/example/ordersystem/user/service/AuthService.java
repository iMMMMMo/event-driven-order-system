package com.example.ordersystem.user.service;

import com.example.ordersystem.user.controller.dto.AuthResponse;
import com.example.ordersystem.user.controller.dto.CreateUserRequest;
import com.example.ordersystem.user.controller.dto.LoginUserRequest;
import com.example.ordersystem.user.controller.dto.UserResponse;

public interface AuthService {

  UserResponse register(CreateUserRequest request);

  AuthResponse login(LoginUserRequest request);

  AuthResponse getCurrentToken();

  UserResponse getCurrentUser();
}
