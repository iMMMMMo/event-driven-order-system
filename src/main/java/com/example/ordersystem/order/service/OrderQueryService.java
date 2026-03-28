package com.example.ordersystem.order.service;

import com.example.ordersystem.order.api.dto.OrderResponse;
import com.example.ordersystem.order.mapper.OrderMapper;
import com.example.ordersystem.order.repository.OrderRepository;
import com.example.ordersystem.shared.exception.ResourceNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

  private final OrderRepository orderRepository;
  private final OrderMapper orderMapper;

  @Cacheable(value = "orders", key = "#id")
  public OrderResponse findCachedOrderOrThrow(UUID id) {
    return orderRepository
        .findById(id)
        .map(orderMapper::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
  }
}
