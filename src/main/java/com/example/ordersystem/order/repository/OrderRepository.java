package com.example.ordersystem.order.repository;

import com.example.ordersystem.order.domain.Order;
import com.example.ordersystem.order.domain.OrderStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, UUID> {

  List<Order> findByStatus(OrderStatus status);

  Page<Order> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

  Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
