package com.example.ordersystem.inventory.service;

import com.example.ordersystem.inventory.domain.InventoryItem;
import com.example.ordersystem.inventory.event.InventoryReservedEvent;
import com.example.ordersystem.inventory.repository.InventoryRepository;
import com.example.ordersystem.shared.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private static final String DEFAULT_PRODUCT = "DEFAULT_PRODUCT";

    private final InventoryRepository inventoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void reserveForOrder(UUID orderId) {

        InventoryItem item = inventoryRepository
                .findByProductName(DEFAULT_PRODUCT)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));

        item.reserve(1);

        eventPublisher.publishEvent(
                new InventoryReservedEvent(orderId)
        );
    }
}