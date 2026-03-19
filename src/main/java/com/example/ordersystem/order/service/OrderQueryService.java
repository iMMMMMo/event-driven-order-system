package com.example.ordersystem.order.service;

import com.example.ordersystem.order.domain.Order;
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

  @Cacheable(value = "orders", key = "#id")
  public Order findCachedOrderOrThrow(UUID id) {
    return orderRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
  }
}
