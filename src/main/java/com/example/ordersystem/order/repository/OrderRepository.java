package com.example.ordersystem.order.repository;

import com.example.ordersystem.order.domain.Order;
import com.example.ordersystem.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByStatus(OrderStatus status);
}