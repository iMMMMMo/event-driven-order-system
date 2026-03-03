package com.example.ordersystem.inventory.service;

import java.util.UUID;

public interface InventoryService {

    void reserveForOrder(UUID orderId);
}