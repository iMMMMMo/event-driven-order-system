package com.example.ordersystem.user.controller;

import com.example.ordersystem.user.controller.dto.*;
import com.example.ordersystem.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public UserResponse register(@RequestBody CreateUserRequest request) {
    return authService.register(request);
  }

  @PostMapping("/login")
  public AuthResponse login(@RequestBody LoginUserRequest request) {
    return authService.login(request);
  }

  @GetMapping("/me")
  public AuthResponse getMyToken() {
    return authService.getCurrentToken();
  }

  @GetMapping("/profile")
  public UserResponse getMyProfile() {
    return authService.getCurrentUser();
  }
}
