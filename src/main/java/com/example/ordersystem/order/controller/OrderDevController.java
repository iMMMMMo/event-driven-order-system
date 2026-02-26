package com.example.ordersystem.order.controller;

import com.example.ordersystem.order.domain.Order;
import com.example.ordersystem.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/dev/orders")
@RequiredArgsConstructor
public class OrderDevController {

    private final OrderService orderService;

    @PostMapping
    public Order create(@RequestParam String email,
                        @RequestParam BigDecimal amount) {
        return orderService.createOrder(email, amount);
    }

    @GetMapping("/{id}")
    public Order get(@PathVariable UUID id) {
        return orderService.getOrder(id);
    }
}