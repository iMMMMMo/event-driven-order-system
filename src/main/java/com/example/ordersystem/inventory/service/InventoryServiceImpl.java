package com.example.ordersystem.inventory.service;

import com.example.ordersystem.inventory.domain.InventoryItem;
import com.example.ordersystem.inventory.repository.InventoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private static final String DEFAULT_PRODUCT = "DEFAULT_PRODUCT";

    private final InventoryRepository inventoryRepository;

    @Override
    public void reserveForOrder() {

        InventoryItem item = inventoryRepository
                .findByProductName(DEFAULT_PRODUCT)
                .orElseThrow(() -> new IllegalStateException("Inventory item not found"));

        item.reserve(1);
    }
}