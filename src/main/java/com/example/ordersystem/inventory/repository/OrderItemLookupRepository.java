package com.example.ordersystem.inventory.repository;

import java.util.List;
import java.util.UUID;

public interface OrderItemLookupRepository {

  List<OrderItemRow> findByOrderId(UUID orderId);

  record OrderItemRow(UUID productId, int quantity) {}
}
