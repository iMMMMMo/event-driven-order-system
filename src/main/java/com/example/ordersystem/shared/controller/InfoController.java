package com.example.ordersystem.shared.controller;

import com.example.ordersystem.shared.api.dto.HealthResponse;
import com.example.ordersystem.shared.api.dto.RootResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Info", description = "Public info endpoints")
public class InfoController {

  @GetMapping("/")
  @Operation(
      summary = "API root",
      description = "Public entrypoint that returns service status and useful links")
  public ResponseEntity<RootResponse> root() {
    return ResponseEntity.ok(
        new RootResponse(
            "Order System API",
            new RootResponse.ApiLinks("/swagger-ui/index.html", "/health"),
            new RootResponse.ExternalLinks("http://localhost:8081")));
  }

  @GetMapping("/health")
  @Operation(summary = "Health check", description = "Return service status and current timestamp")
  public ResponseEntity<HealthResponse> health() {
    return ResponseEntity.ok(new HealthResponse("UP", Instant.now().toString()));
  }
}
