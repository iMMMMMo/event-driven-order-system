package com.example.ordersystem.inventory.repository;

import com.example.ordersystem.inventory.domain.InventoryItem;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<InventoryItem, UUID> {

  Optional<InventoryItem> findByProductName(String productName);

  Page<InventoryItem> findByCategory(String category, Pageable pageable);
}
