package com.example.ordersystem.order.service;

import com.example.ordersystem.order.domain.Order;

import java.math.BigDecimal;
import java.util.UUID;

public interface OrderService {

    Order createOrder(String email, BigDecimal amount);

    Order getOrder(UUID id);
}
