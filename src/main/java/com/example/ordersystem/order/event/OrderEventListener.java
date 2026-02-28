package com.example.ordersystem.order.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventListener {

    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Order created: {}", event.getOrderId());
    }

    @EventListener
    public void handleOrderPaid(OrderPaidEvent event) {
        log.info("Order paid: {}", event.getOrderId());
    }
}