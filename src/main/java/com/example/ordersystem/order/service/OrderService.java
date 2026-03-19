package com.example.ordersystem.order.service;

import com.example.ordersystem.order.controller.dto.OrderResponse;
import java.math.BigDecimal;
import java.util.UUID;

public interface OrderService {

  OrderResponse createOrder(String email, BigDecimal amount);

  OrderResponse getOrder(UUID id, String email);

  OrderResponse pay(UUID id, BigDecimal amount, String email);

  OrderResponse markAsPaid(UUID id);

  OrderResponse completeOrder(UUID id);

  OrderResponse cancelOrder(UUID id);
}
