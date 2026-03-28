package com.example.ordersystem.inventory.service;

import com.example.ordersystem.inventory.api.dto.CreateProductRequest;
import com.example.ordersystem.inventory.api.dto.ProductResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryService {

  record ReservationLine(UUID productId, int quantity) {}

  void reserveForOrder(UUID orderId);

  void reserveForOrder(UUID orderId, List<ReservationLine> items);

  Page<ProductResponse> getProducts(String category, Pageable pageable);

  ProductResponse getProduct(UUID id);

  ProductResponse createProduct(CreateProductRequest request);
}
