package com.example.ordersystem.inventory.event;

import com.example.ordersystem.inventory.service.InventoryService;
import com.example.ordersystem.payment.event.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryEventListener {

    private final InventoryService inventoryService;

    @EventListener
    public void handlePaymentSuccess(PaymentSucceededEvent event) {
        inventoryService.reserveForOrder();
    }
}