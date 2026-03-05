package com.example.ordersystem.user.service;

import com.example.ordersystem.shared.security.JwtService;
import com.example.ordersystem.user.domain.UserRole;
import com.example.ordersystem.user.domain.User;
import com.example.ordersystem.user.controller.dto.*;
import com.example.ordersystem.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public UserResponse register(CreateUserRequest request) {

        if (repository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.USER)
                .build();

        repository.save(user);

        return new UserResponse(user.getEmail(), user.getRole());
    }

    @Override
    public AuthResponse login(LoginUserRequest request) {

        User user = repository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return new AuthResponse(jwtService.generateToken(user));
    }

    @Override
    public UserResponse getCurrentUser() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserResponse(user.getEmail(), user.getRole());
    }

    @Override
    public AuthResponse getCurrentToken() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new AuthResponse(jwtService.generateToken(user));
    }
}