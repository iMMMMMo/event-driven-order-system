package com.example.ordersystem.order.controller;

import com.example.ordersystem.order.controller.dto.CreateOrderRequest;
import com.example.ordersystem.order.controller.dto.OrderResponse;
import com.example.ordersystem.order.service.OrderService;
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
public class OrderController {

  private final OrderService orderService;

  @PostMapping
  public ResponseEntity<OrderResponse> createOrder(
      @Valid @RequestBody CreateOrderRequest request, java.security.Principal principal) {

    OrderResponse response = orderService.createOrder(principal.getName(), request.totalAmount());

    return ResponseEntity.created(URI.create("/api/orders/" + response.id())).body(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
    return ResponseEntity.ok(orderService.getOrder(id));
  }

  @PatchMapping("/{id}/pay")
  public ResponseEntity<OrderResponse> pay(@PathVariable UUID id, @RequestParam BigDecimal amount) {

    return ResponseEntity.ok(orderService.pay(id, amount));
  }
}
