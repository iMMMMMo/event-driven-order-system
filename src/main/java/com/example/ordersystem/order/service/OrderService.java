package com.example.ordersystem.order.service;

import com.example.ordersystem.order.api.dto.CreateOrderItemRequest;
import com.example.ordersystem.order.api.dto.OrderResponse;
import com.example.ordersystem.order.api.dto.StripePaymentRequestResponse;
import com.example.ordersystem.order.api.dto.StripePaymentStatusResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

  Page<OrderResponse> getOrdersForUser(String email, Pageable pageable);

  Page<OrderResponse> getAllOrders(Pageable pageable);

  OrderResponse createOrder(String email, List<CreateOrderItemRequest> items);

  OrderResponse getOrder(UUID id, String email);

  OrderResponse pay(UUID id, BigDecimal amount, String email);

  StripePaymentRequestResponse requestStripeCheckout(UUID id, String email);

  StripePaymentStatusResponse getStripeCheckoutStatus(UUID id, String email);

  OrderResponse markAsPaid(UUID id);

  OrderResponse completeOrder(UUID id);

  OrderResponse cancelOrder(UUID id);
}
