package com.example.ordersystem.order.controller;

import com.example.ordersystem.order.api.dto.CreateOrderRequest;
import com.example.ordersystem.order.api.dto.OrderResponse;
import com.example.ordersystem.order.api.dto.StripePaymentRequestResponse;
import com.example.ordersystem.order.api.dto.StripePaymentStatusResponse;
import com.example.ordersystem.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management API")
public class OrderController {

  private final OrderService orderService;

  @GetMapping
  @Operation(
      summary = "Get current user orders",
      description = "Retrieve orders for the current user")
  public ResponseEntity<Page<OrderResponse>> getCurrentUserOrders(
      @ParameterObject
          @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable,
      java.security.Principal principal) {
    return ResponseEntity.ok(orderService.getOrdersForUser(principal.getName(), pageable));
  }

  @GetMapping("/admin")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Get all orders", description = "Retrieve all orders (Admin only)")
  public ResponseEntity<Page<OrderResponse>> getAllOrders(
      @ParameterObject
          @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {
    return ResponseEntity.ok(orderService.getAllOrders(pageable));
  }

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

  @PostMapping("/{id}/payments/stripe")
  @Operation(
      summary = "Request Stripe payment",
      description = "Initiate Stripe Checkout asynchronously and return polling URL")
  public ResponseEntity<StripePaymentRequestResponse> requestStripePayment(
      @PathVariable UUID id, java.security.Principal principal) {

    return ResponseEntity.accepted()
        .body(orderService.requestStripeCheckout(id, principal.getName()));
  }

  @GetMapping("/{id}/payments/stripe")
  @Operation(
      summary = "Get Stripe payment status",
      description = "Poll current Stripe checkout status for an order")
  public ResponseEntity<StripePaymentStatusResponse> getStripePaymentStatus(
      @PathVariable UUID id, java.security.Principal principal) {

    return ResponseEntity.ok(orderService.getStripeCheckoutStatus(id, principal.getName()));
  }
}
