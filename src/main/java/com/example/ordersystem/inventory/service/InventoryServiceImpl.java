package com.example.ordersystem.inventory.service;

import com.example.ordersystem.inventory.controller.dto.CreateProductRequest;
import com.example.ordersystem.inventory.controller.dto.ProductResponse;
import com.example.ordersystem.inventory.domain.InventoryItem;
import com.example.ordersystem.inventory.event.InventoryReservedEvent;
import com.example.ordersystem.inventory.repository.InventoryRepository;
import com.example.ordersystem.shared.event.DomainEventPublisher;
import com.example.ordersystem.shared.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

  private static final String DEFAULT_PRODUCT = "DEFAULT_PRODUCT";

  private final InventoryRepository inventoryRepository;
  private final DomainEventPublisher eventPublisher;

  @Override
  public void reserveForOrder(UUID orderId) {

    InventoryItem item =
        inventoryRepository
            .findByProductName(DEFAULT_PRODUCT)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));

    item.reserve(1);

    eventPublisher.publish(new InventoryReservedEvent(orderId));
  }

  @Override
  public Page<ProductResponse> getProducts(String category, Pageable pageable) {
    Page<InventoryItem> itemsPage;
    if (category != null && !category.isBlank()) {
      itemsPage = inventoryRepository.findByCategory(category, pageable);
    } else {
      itemsPage = inventoryRepository.findAll(pageable);
    }
    return itemsPage.map(this::mapToResponse);
  }

  @Override
  public ProductResponse getProduct(UUID id) {
    return inventoryRepository
        .findById(id)
        .map(this::mapToResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
  }

  @Override
  public ProductResponse createProduct(CreateProductRequest request) {
    InventoryItem item =
        InventoryItem.create(
            request.productName(),
            request.price(),
            request.description(),
            request.category(),
            request.quantity());

    InventoryItem saved = inventoryRepository.save(item);
    return mapToResponse(saved);
  }

  private ProductResponse mapToResponse(InventoryItem item) {
    return new ProductResponse(
        item.getId(),
        item.getProductName(),
        item.getPrice(),
        item.getDescription(),
        item.getCategory(),
        item.available());
  }
}
