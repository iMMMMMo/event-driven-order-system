package com.example.ordersystem.order.service;

import com.example.ordersystem.order.domain.Order;
import com.example.ordersystem.order.event.OrderCreatedEvent;
import com.example.ordersystem.order.event.OrderPaidEvent;
import com.example.ordersystem.order.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Order createOrder(String email, BigDecimal amount) {
        Order order = Order.create(email, amount);
        Order saved = orderRepository.save(order);

        eventPublisher.publishEvent(
                new OrderCreatedEvent(
                        saved.getId(),
                        saved.getCustomerEmail(),
                        saved.getTotalAmount()
                )
        );

        return saved;
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Order getOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    @Override
    public Order markAsPaid(UUID id) {
        Order order = getOrder(id);
        order.markAsPaid();

        eventPublisher.publishEvent(
                new OrderPaidEvent(order.getId())
        );

        return order;
    }
}