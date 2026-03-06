package com.example.ordersystem.order.service;

import com.example.ordersystem.order.controller.dto.OrderResponse;
import com.example.ordersystem.order.domain.Order;
import com.example.ordersystem.order.event.OrderCreatedEvent;
import com.example.ordersystem.order.event.OrderPaidEvent;
import com.example.ordersystem.order.mapper.OrderMapper;
import com.example.ordersystem.order.repository.OrderRepository;
import com.example.ordersystem.user.domain.User;
import com.example.ordersystem.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public OrderResponse createOrder(String email, BigDecimal amount) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = Order.create(user.getId(), user.getEmail(), amount);
        Order saved = orderRepository.save(order);

        eventPublisher.publishEvent(
                new OrderCreatedEvent(
                        saved.getId(),
                        saved.getCustomerEmail(),
                        saved.getTotalAmount()
                )
        );

        return orderMapper.toResponse(saved);
    }

    @Override
    @Cacheable(value = "orders", key = "#id")
    @Transactional(Transactional.TxType.SUPPORTS)
    public OrderResponse getOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        return orderMapper.toResponse(order);
    }

    @Override
    @CacheEvict(value = "orders", key = "#id")
    public OrderResponse markAsPaid(UUID id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        order.markAsPaid();

        eventPublisher.publishEvent(
                new OrderPaidEvent(order.getId())
        );

        return orderMapper.toResponse(order);
    }
}