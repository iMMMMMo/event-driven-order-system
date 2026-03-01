package com.example.ordersystem.payment.event;

import com.example.ordersystem.order.event.OrderCreatedEvent;
import com.example.ordersystem.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentService paymentService;

    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {

        String idempotencyKey = "payment-" + event.getOrderId();

        paymentService.processPayment(
                event.getOrderId(),
                idempotencyKey
        );
    }
}