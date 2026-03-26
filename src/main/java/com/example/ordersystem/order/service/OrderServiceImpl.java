package com.example.ordersystem.order.service;

import com.example.ordersystem.inventory.domain.InventoryItem;
import com.example.ordersystem.inventory.repository.InventoryRepository;
import com.example.ordersystem.order.controller.dto.CreateOrderItemRequest;
import com.example.ordersystem.order.controller.dto.OrderResponse;
import com.example.ordersystem.order.controller.dto.StripePaymentRequestResponse;
import com.example.ordersystem.order.controller.dto.StripePaymentStatusResponse;
import com.example.ordersystem.order.domain.Order;
import com.example.ordersystem.order.domain.OrderStatus;
import com.example.ordersystem.order.domain.StripeCheckoutRequest;
import com.example.ordersystem.order.event.OrderCancelledEvent;
import com.example.ordersystem.order.event.OrderCompletedEvent;
import com.example.ordersystem.order.event.OrderCreatedEvent;
import com.example.ordersystem.order.event.OrderPaidEvent;
import com.example.ordersystem.order.event.OrderPaymentRequestedEvent;
import com.example.ordersystem.order.event.OrderStripeCheckoutRequestedEvent;
import com.example.ordersystem.order.mapper.OrderMapper;
import com.example.ordersystem.order.repository.OrderRepository;
import com.example.ordersystem.order.repository.StripeCheckoutRequestRepository;
import com.example.ordersystem.shared.event.DomainEventPublisher;
import com.example.ordersystem.shared.exception.ConflictException;
import com.example.ordersystem.shared.exception.ResourceNotFoundException;
import com.example.ordersystem.user.domain.User;
import com.example.ordersystem.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

  private final UserRepository userRepository;
  private final OrderRepository orderRepository;
  private final OrderQueryService orderQueryService;
  private final InventoryRepository inventoryRepository;
  private final OrderMapper orderMapper;
  private final DomainEventPublisher eventPublisher;
  private final StripeCheckoutRequestRepository stripeCheckoutRequestRepository;

  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  public Page<OrderResponse> getOrdersForUser(String email, Pageable pageable) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    return orderRepository
        .findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
        .map(orderMapper::toResponse);
  }

  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  public Page<OrderResponse> getAllOrders(Pageable pageable) {
    return orderRepository.findAllByOrderByCreatedAtDesc(pageable).map(orderMapper::toResponse);
  }

  @Override
  public OrderResponse createOrder(String email, List<CreateOrderItemRequest> items) {

    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Map<UUID, Integer> quantitiesByProductId = new LinkedHashMap<>();
    for (CreateOrderItemRequest item : items) {
      quantitiesByProductId.merge(item.productId(), item.quantity(), Integer::sum);
    }

    List<InventoryItem> inventoryItems =
        inventoryRepository.findAllById(quantitiesByProductId.keySet());
    if (inventoryItems.size() != quantitiesByProductId.size()) {
      throw new ResourceNotFoundException("Product not found");
    }

    Map<UUID, InventoryItem> inventoryById = new LinkedHashMap<>();
    for (InventoryItem inventoryItem : inventoryItems) {
      inventoryById.put(inventoryItem.getId(), inventoryItem);
    }

    BigDecimal totalAmount = BigDecimal.ZERO;
    for (Map.Entry<UUID, Integer> entry : quantitiesByProductId.entrySet()) {
      InventoryItem inventoryItem = inventoryById.get(entry.getKey());
      int quantity = entry.getValue();

      if (inventoryItem.available() < quantity) {
        throw new ConflictException("Not enough stock");
      }

      totalAmount =
          totalAmount.add(inventoryItem.getPrice().multiply(BigDecimal.valueOf(quantity)));
    }

    Order order = Order.create(user.getId(), user.getEmail(), totalAmount);
    for (Map.Entry<UUID, Integer> entry : quantitiesByProductId.entrySet()) {
      InventoryItem inventoryItem = inventoryById.get(entry.getKey());
      order.addItem(inventoryItem.getId(), entry.getValue(), inventoryItem.getPrice());
    }
    Order saved = orderRepository.save(order);

    eventPublisher.publish(
        new OrderCreatedEvent(saved.getId(), saved.getCustomerEmail(), saved.getTotalAmount()));

    return orderMapper.toResponse(saved);
  }

  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  public OrderResponse getOrder(UUID id, String email) {
    OrderResponse response = orderQueryService.findCachedOrderOrThrow(id);
    if (!response.customerEmail().equals(email)) {
      throw new ResourceNotFoundException("Order not found");
    }
    return response;
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
  public StripePaymentRequestResponse requestStripeCheckout(UUID id, String email) {
    Order order = findOrder(id);
    if (!order.getCustomerEmail().equals(email)) {
      throw new ResourceNotFoundException("Order not found");
    }
    if (order.getStatus() != OrderStatus.CREATED) {
      throw new ConflictException("Only CREATED orders can be paid");
    }

    StripeCheckoutRequest existingRequest =
        stripeCheckoutRequestRepository.findByOrderId(order.getId()).orElse(null);
    if (existingRequest == null) {
      stripeCheckoutRequestRepository.save(StripeCheckoutRequest.requested(order.getId()));
      eventPublisher.publish(
          new OrderStripeCheckoutRequestedEvent(order.getId(), order.getTotalAmount()));
    }
    String pollUrl = "/api/orders/" + order.getId() + "/payments/stripe";
    return new StripePaymentRequestResponse(order.getId(), "REQUESTED", pollUrl);
  }

  @Override
  @Transactional(Transactional.TxType.SUPPORTS)
  public StripePaymentStatusResponse getStripeCheckoutStatus(UUID id, String email) {
    Order order = findOrder(id);
    if (!order.getCustomerEmail().equals(email)) {
      throw new ResourceNotFoundException("Order not found");
    }

    return stripeCheckoutRequestRepository
        .findByOrderId(order.getId())
        .map(
            r ->
                new StripePaymentStatusResponse(
                    order.getId(), r.getStatus().name(), r.getCheckoutUrl()))
        .orElseGet(() -> new StripePaymentStatusResponse(order.getId(), "NOT_REQUESTED", null));
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
