package com.example.ordersystem.inventory.service;

import com.example.ordersystem.inventory.controller.dto.CreateProductRequest;
import com.example.ordersystem.inventory.controller.dto.ProductResponse;
import com.example.ordersystem.inventory.domain.InventoryItem;
import com.example.ordersystem.inventory.event.InventoryReservedEvent;
import com.example.ordersystem.inventory.repository.InventoryRepository;
import com.example.ordersystem.inventory.repository.OrderItemLookupRepository;
import com.example.ordersystem.inventory.repository.OrderItemLookupRepository.OrderItemRow;
import com.example.ordersystem.shared.event.DomainEventPublisher;
import com.example.ordersystem.shared.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InventoryServiceImpl implements InventoryService {

  private static final String PRODUCT_BY_ID_CACHE = "productById";

  private final InventoryRepository inventoryRepository;
  private final OrderItemLookupRepository orderItemLookupRepository;
  private final DomainEventPublisher eventPublisher;

  @Override
  @CacheEvict(value = PRODUCT_BY_ID_CACHE, allEntries = true)
  public void reserveForOrder(UUID orderId) {
    reserveForOrder(orderId, List.of());
  }

  @Override
  @CacheEvict(value = PRODUCT_BY_ID_CACHE, allEntries = true)
  public void reserveForOrder(UUID orderId, List<ReservationLine> items) {

    if (items == null || items.isEmpty()) {
      log.info(
          "OrderPaidEvent missing items; using fallback order_items lookup for orderId={}",
          orderId);
      for (OrderItemRow item : orderItemLookupRepository.findByOrderId(orderId)) {
        reserve(item.productId(), item.quantity());
      }
      eventPublisher.publish(new InventoryReservedEvent(orderId));
      return;
    }

    for (ReservationLine item : items) {
      reserve(item.productId(), item.quantity());
    }

    eventPublisher.publish(new InventoryReservedEvent(orderId));
  }

  private void reserve(UUID productId, int quantity) {
    InventoryItem inventoryItem =
        inventoryRepository
            .findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));

    inventoryItem.reserve(quantity);
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
  @Cacheable(value = PRODUCT_BY_ID_CACHE, key = "#id")
  public ProductResponse getProduct(UUID id) {
    return inventoryRepository
        .findById(id)
        .map(this::mapToResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
  }

  @Override
  @CacheEvict(value = PRODUCT_BY_ID_CACHE, allEntries = true)
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
