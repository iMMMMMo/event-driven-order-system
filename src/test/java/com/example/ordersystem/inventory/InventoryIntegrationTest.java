package com.example.ordersystem.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.ordersystem.config.AbstractIntegrationTest;
import com.example.ordersystem.inventory.domain.InventoryItem;
import com.example.ordersystem.inventory.repository.InventoryRepository;
import com.example.ordersystem.inventory.service.InventoryService;
import com.example.ordersystem.order.domain.Order;
import com.example.ordersystem.order.repository.OrderRepository;
import com.example.ordersystem.shared.exception.ConflictException;
import com.example.ordersystem.user.domain.User;
import com.example.ordersystem.user.domain.UserRole;
import com.example.ordersystem.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

public class InventoryIntegrationTest extends AbstractIntegrationTest {

  @Autowired private InventoryService inventoryService;

  @MockitoSpyBean private InventoryRepository inventoryRepository;

  @Autowired private OrderRepository orderRepository;

  @Autowired private UserRepository userRepository;

  private User testUser;

  @BeforeEach
  void resetInventory() {
    orderRepository.deleteAll();
    userRepository.deleteAll();
    inventoryRepository.deleteAll();
    testUser =
        userRepository.save(
            User.builder()
                .email("inventory-test-" + UUID.randomUUID() + "@example.com")
                .password("password")
                .role(UserRole.USER)
                .build());
  }

  @Test
  void shouldReserveInventoryForOrder() {
    InventoryItem product =
        inventoryRepository.save(
            InventoryItem.create(
                "PRODUCT_" + UUID.randomUUID(), new BigDecimal("10.00"), "d", "c", 10));

    Order order = Order.create(testUser.getId(), testUser.getEmail(), new BigDecimal("20.00"));
    order.addItem(product.getId(), 2, product.getPrice());
    Order savedOrder = orderRepository.save(order);

    inventoryService.reserveForOrder(savedOrder.getId());

    InventoryItem item = inventoryRepository.findById(product.getId()).orElseThrow();

    assertThat(item.getReserved()).isEqualTo(2);
    assertThat(item.available()).isEqualTo(8);
  }

  @Test
  void shouldNotReserveAnythingWhenOrderHasNoLines() {
    InventoryItem product =
        inventoryRepository.save(
            InventoryItem.create(
                "PRODUCT_" + UUID.randomUUID(), new BigDecimal("10.00"), "d", "c", 10));

    Order order = Order.create(testUser.getId(), testUser.getEmail(), new BigDecimal("0.00"));
    Order savedOrder = orderRepository.save(order);

    inventoryService.reserveForOrder(savedOrder.getId());

    InventoryItem item = inventoryRepository.findById(product.getId()).orElseThrow();
    assertThat(item.getReserved()).isZero();
  }

  @Test
  void shouldThrowConflictWhenStockIsUnavailable() {
    InventoryItem product =
        inventoryRepository.save(
            InventoryItem.create(
                "PRODUCT_" + UUID.randomUUID(), new BigDecimal("10.00"), "d", "c", 0));

    Order order = Order.create(testUser.getId(), testUser.getEmail(), new BigDecimal("10.00"));
    order.addItem(product.getId(), 1, product.getPrice());
    Order savedOrder = orderRepository.save(order);

    assertThatThrownBy(() -> inventoryService.reserveForOrder(savedOrder.getId()))
        .isInstanceOf(ConflictException.class)
        .hasMessage("Not enough stock");
  }

  @Test
  void shouldCacheSingleProductById() {
    InventoryItem product =
        inventoryRepository.save(
            InventoryItem.create(
                "PRODUCT_" + UUID.randomUUID(), new BigDecimal("10.00"), "d", "c", 10));

    clearInvocations(inventoryRepository);

    inventoryService.getProduct(product.getId());
    inventoryService.getProduct(product.getId());

    verify(inventoryRepository, times(1)).findById(product.getId());
  }
}
