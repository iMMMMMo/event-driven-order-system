package com.example.ordersystem.payment.event;

import com.example.ordersystem.order.event.OrderPaymentRequestedEvent;
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
    public void handlePaymentRequested(OrderPaymentRequestedEvent event) {

        paymentService.processPayment(
                event.getOrderId(),
                event.getAmount(),
                event.getExpectedAmount()
        );
    }
}