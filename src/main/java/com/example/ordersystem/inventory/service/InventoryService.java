package com.example.ordersystem.inventory.service;

import com.example.ordersystem.inventory.controller.dto.CreateProductRequest;
import com.example.ordersystem.inventory.controller.dto.ProductResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryService {

  void reserveForOrder(UUID orderId);

  Page<ProductResponse> getProducts(String category, Pageable pageable);

  ProductResponse getProduct(UUID id);

  ProductResponse createProduct(CreateProductRequest request);
}
