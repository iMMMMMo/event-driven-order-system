package com.example.ordersystem.order.event;

import com.example.ordersystem.order.service.OrderService;
import com.example.ordersystem.payment.event.PaymentFailedEvent;
import com.example.ordersystem.payment.event.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderService orderService;

    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {

        log.info("Order created: {}", event.getOrderId());
    }

    @EventListener
    public void handlePaymentSucceeded(PaymentSucceededEvent event) {
        log.info("Payment success event received for order: {}.", event.getOrderId());
        orderService.markAsPaid(event.getOrderId());
    }

    @EventListener
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.warn("Payment FAILED for order: {}. Reason: {}. Cancelling order.",
                event.getOrderId(), event.getReason());
        orderService.cancelOrder(event.getOrderId());
    }

    @EventListener
    public void handleOrderPaid(OrderPaidEvent event) {

        log.info("Order paid: {}", event.getOrderId());
    }

    @EventListener
    public void handleOrderCancelled(OrderCancelledEvent event) {

        log.info("Order cancelled: {}", event.getOrderId());
    }
}