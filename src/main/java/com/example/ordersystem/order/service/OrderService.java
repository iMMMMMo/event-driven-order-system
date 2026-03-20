package com.example.ordersystem.order.service;

import com.example.ordersystem.order.controller.dto.CreateOrderItemRequest;
import com.example.ordersystem.order.controller.dto.OrderResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface OrderService {

  OrderResponse createOrder(String email, List<CreateOrderItemRequest> items);

  OrderResponse getOrder(UUID id, String email);

  OrderResponse pay(UUID id, BigDecimal amount, String email);

  OrderResponse markAsPaid(UUID id);

  OrderResponse completeOrder(UUID id);

  OrderResponse cancelOrder(UUID id);
}
