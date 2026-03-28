package com.example.ordersystem.user.service;

import com.example.ordersystem.shared.security.JwtService;
import com.example.ordersystem.user.api.dto.*;
import com.example.ordersystem.user.domain.User;
import com.example.ordersystem.user.domain.UserRole;
import com.example.ordersystem.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserRepository repository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  @Override
  public UserResponse register(CreateUserRequest request) {

    if (repository.findByEmail(request.email()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
    }

    User user =
        User.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .role(UserRole.USER)
            .build();

    repository.save(user);

    return new UserResponse(user.getEmail(), user.getRole());
  }

  @Override
  public AuthResponse login(LoginUserRequest request) {

    User user =
        repository
            .findByEmail(request.email())
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new BadCredentialsException("Invalid credentials");
    }

    return new AuthResponse(jwtService.generateToken(user));
  }

  @Override
  public UserResponse getCurrentUser() {

    String email = getAuthenticatedEmail();

    User user =
        repository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    return new UserResponse(user.getEmail(), user.getRole());
  }

  @Override
  public AuthResponse getCurrentToken() {

    String email = getAuthenticatedEmail();

    User user =
        repository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    return new AuthResponse(jwtService.generateToken(user));
  }

  private String getAuthenticatedEmail() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new BadCredentialsException("Authentication required");
    }

    String email = authentication.getName();
    if (email == null || email.isBlank()) {
      throw new BadCredentialsException("Invalid authentication context");
    }

    return email;
  }
}
