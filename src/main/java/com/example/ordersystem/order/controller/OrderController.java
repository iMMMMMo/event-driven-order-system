package com.example.ordersystem.order.controller;

import com.example.ordersystem.order.controller.dto.CreateOrderRequest;
import com.example.ordersystem.order.controller.dto.OrderResponse;
import com.example.ordersystem.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management API")
public class OrderController {

  private final OrderService orderService;

  @PostMapping
  @Operation(summary = "Create order", description = "Create a new order for the current user")
  public ResponseEntity<OrderResponse> createOrder(
      @Valid @RequestBody CreateOrderRequest request, java.security.Principal principal) {

    OrderResponse response = orderService.createOrder(principal.getName(), request.items());

    return ResponseEntity.created(URI.create("/api/orders/" + response.id())).body(response);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get order by ID", description = "Retrieve order details by ID")
  public ResponseEntity<OrderResponse> getOrder(
      @PathVariable UUID id, java.security.Principal principal) {
    return ResponseEntity.ok(orderService.getOrder(id, principal.getName()));
  }

  @PatchMapping("/{id}/pay")
  @Operation(summary = "Pay for order", description = "Pay for an order with a specified amount")
  public ResponseEntity<OrderResponse> pay(
      @PathVariable UUID id, @RequestParam BigDecimal amount, java.security.Principal principal) {

    return ResponseEntity.ok(orderService.pay(id, amount, principal.getName()));
  }
}
