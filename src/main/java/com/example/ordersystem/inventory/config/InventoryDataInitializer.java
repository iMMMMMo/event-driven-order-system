package com.example.ordersystem.inventory.config;

import com.example.ordersystem.inventory.domain.InventoryItem;
import com.example.ordersystem.inventory.repository.InventoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryDataInitializer {

    private final InventoryRepository repository;

    @PostConstruct
    public void init() {
        repository.findByProductName("DEFAULT_PRODUCT")
                .orElseGet(() ->
                        repository.save(
                                InventoryItem.create("DEFAULT_PRODUCT", 100)
                        )
                );
    }
}