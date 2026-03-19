package com.example.ordersystem.order.service;

import com.example.ordersystem.order.controller.dto.OrderResponse;
import com.example.ordersystem.order.domain.Order;
import com.example.ordersystem.order.domain.OrderStatus;
import com.example.ordersystem.order.event.OrderCancelledEvent;
import com.example.ordersystem.order.event.OrderCompletedEvent;
import com.example.ordersystem.order.event.OrderCreatedEvent;
import com.example.ordersystem.order.event.OrderPaidEvent;
import com.example.ordersystem.order.event.OrderPaymentRequestedEvent;
import com.example.ordersystem.order.mapper.OrderMapper;
import com.example.ordersystem.order.repository.OrderRepository;
import com.example.ordersystem.shared.event.DomainEventPublisher;
import com.example.ordersystem.shared.exception.ConflictException;
import com.example.ordersystem.shared.exception.ResourceNotFoundException;
import com.example.ordersystem.user.domain.User;
import com.example.ordersystem.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

  private final UserRepository userRepository;
  private final OrderRepository orderRepository;
  private final OrderQueryService orderQueryService;
  private final OrderMapper orderMapper;
  private final DomainEventPublisher eventPublisher;

  @Override
  public OrderResponse createOrder(String email, BigDecimal amount) {

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Order order = Order.create(user.getId(), user.getEmail(), amount);
    Order saved = orderRepository.save(order);

    eventPublisher.publish(
        new OrderCreatedEvent(saved.getId(), saved.getCustomerEmail(), saved.getTotalAmount()));

    return orderMapper.toResponse(saved);
  }

  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  public OrderResponse getOrder(UUID id, String email) {
    Order order = orderQueryService.findCachedOrderOrThrow(id);
    if (!order.getCustomerEmail().equals(email)) {
      throw new ResourceNotFoundException("Order not found");
    }
    return orderMapper.toResponse(order);
  }

  @Override
  public OrderResponse pay(UUID id, BigDecimal amount, String email) {
    Order order = findOrder(id);
    if (!order.getCustomerEmail().equals(email)) {
      throw new ResourceNotFoundException("Order not found");
    }
    if (order.getStatus() != OrderStatus.CREATED) {
      throw new ConflictException("Only CREATED orders can be paid");
    }

    eventPublisher.publish(
        new OrderPaymentRequestedEvent(order.getId(), amount, order.getTotalAmount()));

    return orderMapper.toResponse(order);
  }

  @Override
  @CacheEvict(value = "orders", key = "#id")
  public OrderResponse markAsPaid(UUID id) {

    Order order = findOrder(id);

    order.markAsPaid();

    eventPublisher.publish(new OrderPaidEvent(order.getId()));

    return orderMapper.toResponse(order);
  }

  @Override
  @CacheEvict(value = "orders", key = "#id")
  public OrderResponse completeOrder(UUID id) {

    Order order = findOrder(id);

    order.complete();

    eventPublisher.publish(new OrderCompletedEvent(order.getId()));

    return orderMapper.toResponse(order);
  }

  @Override
  @CacheEvict(value = "orders", key = "#id")
  public OrderResponse cancelOrder(UUID id) {

    Order order = findOrder(id);

    order.cancel();

    eventPublisher.publish(new OrderCancelledEvent(order.getId()));

    return orderMapper.toResponse(order);
  }

  private Order findOrder(UUID id) {
    return orderRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
  }
}
