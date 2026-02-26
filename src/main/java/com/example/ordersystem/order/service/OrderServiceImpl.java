package com.example.ordersystem.order.service;

import com.example.ordersystem.order.domain.Order;
import com.example.ordersystem.order.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    public Order createOrder(String email, BigDecimal amount) {
        Order order = Order.create(email, amount);
        return orderRepository.save(order);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Order getOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }
}