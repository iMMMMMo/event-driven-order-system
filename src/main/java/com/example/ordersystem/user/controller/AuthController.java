package com.example.ordersystem.user.controller;

import com.example.ordersystem.user.api.dto.*;
import com.example.ordersystem.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and user profile API")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  @Operation(summary = "Register", description = "Create a new user account")
  public UserResponse register(@RequestBody CreateUserRequest request) {
    return authService.register(request);
  }

  @PostMapping("/login")
  @Operation(summary = "Login", description = "Authenticate a user and return an access token")
  public AuthResponse login(@RequestBody LoginUserRequest request) {
    return authService.login(request);
  }

  @GetMapping("/me")
  @Operation(summary = "Get my token", description = "Retrieve the current access token")
  public AuthResponse getMyToken() {
    return authService.getCurrentToken();
  }

  @GetMapping("/profile")
  @Operation(summary = "Get my profile", description = "Retrieve details for the current user")
  public UserResponse getMyProfile() {
    return authService.getCurrentUser();
  }
}
