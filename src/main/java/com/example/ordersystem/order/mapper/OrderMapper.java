package com.example.ordersystem.order.mapper;

import com.example.ordersystem.order.controller.dto.OrderItemResponse;
import com.example.ordersystem.order.controller.dto.OrderResponse;
import com.example.ordersystem.order.domain.Order;
import com.example.ordersystem.order.domain.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

  @Mapping(target = "items", source = "items")
  OrderResponse toResponse(Order order);

  OrderItemResponse toItemResponse(OrderItem item);
}
