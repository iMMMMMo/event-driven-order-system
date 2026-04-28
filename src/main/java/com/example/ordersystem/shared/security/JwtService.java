package com.example.ordersystem.shared.security;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
  String extractUsername(String token);

  String generateToken(UserDetails userDetails);

  boolean isTokenExpired(String token);

  boolean isTokenValid(String token, UserDetails userDetails);
}
