package com.example.ordersystem.shared.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health", description = "Service health endpoints")
public class HealthController {

  @GetMapping("/health")
  @Operation(summary = "Health check", description = "Return service status and current timestamp")
  public ResponseEntity<Map<String, Object>> health() {
    return ResponseEntity.ok(Map.of("status", "UP", "timestamp", Instant.now().toString()));
  }
}
