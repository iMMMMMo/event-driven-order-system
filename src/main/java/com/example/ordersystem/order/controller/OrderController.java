package com.example.ordersystem.order.controller;

import com.example.ordersystem.order.controller.dto.CreateOrderRequest;
import com.example.ordersystem.order.controller.dto.OrderResponse;
import com.example.ordersystem.order.domain.Order;
import com.example.ordersystem.order.mapper.OrderMapper;
import com.example.ordersystem.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        Order order = orderService.createOrder(
                request.customerEmail(),
                request.totalAmount()
        );

        return ResponseEntity
                .created(URI.create("/api/orders/" + order.getId()))
                .body(orderMapper.toResponse(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        Order order = orderService.getOrder(id);
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<OrderResponse> markAsPaid(@PathVariable UUID id) {
        Order order = orderService.markAsPaid(id);
        return ResponseEntity.ok(orderMapper.toResponse(order));
    }
}