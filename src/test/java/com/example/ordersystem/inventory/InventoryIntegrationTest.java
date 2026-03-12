package com.example.ordersystem.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.ordersystem.config.AbstractIntegrationTest;
import com.example.ordersystem.inventory.domain.InventoryItem;
import com.example.ordersystem.inventory.repository.InventoryRepository;
import com.example.ordersystem.inventory.service.InventoryService;
import com.example.ordersystem.shared.exception.ConflictException;
import com.example.ordersystem.shared.exception.ResourceNotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class InventoryIntegrationTest extends AbstractIntegrationTest {

  @Autowired private InventoryService inventoryService;

  @Autowired private InventoryRepository inventoryRepository;

  @BeforeEach
  void resetInventory() {
    inventoryRepository.deleteAll();
    inventoryRepository.save(InventoryItem.create("DEFAULT_PRODUCT", 10));
  }

  @Test
  void shouldReserveInventoryForOrder() {
    inventoryService.reserveForOrder(UUID.randomUUID());

    InventoryItem item = inventoryRepository.findByProductName("DEFAULT_PRODUCT").orElseThrow();

    assertThat(item.getReserved()).isEqualTo(1);
    assertThat(item.available()).isEqualTo(9);
  }

  @Test
  void shouldThrowNotFoundWhenInventoryItemIsMissing() {
    inventoryRepository.deleteAll();

    assertThatThrownBy(() -> inventoryService.reserveForOrder(UUID.randomUUID()))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Inventory item not found");
  }

  @Test
  void shouldThrowConflictWhenStockIsUnavailable() {
    inventoryRepository.deleteAll();
    inventoryRepository.save(InventoryItem.create("DEFAULT_PRODUCT", 0));

    assertThatThrownBy(() -> inventoryService.reserveForOrder(UUID.randomUUID()))
        .isInstanceOf(ConflictException.class)
        .hasMessage("Not enough stock");
  }
}
