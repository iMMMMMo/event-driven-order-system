package com.example.ordersystem.order.event;

import com.example.ordersystem.inventory.event.InventoryReservedEvent;
import com.example.ordersystem.order.repository.OrderRepository;
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
  private final OrderRepository orderRepository;

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
    log.warn(
        "Payment FAILED for order: {}. Reason: {}. Cancelling order.",
        event.getOrderId(),
        event.getReason());
    orderService.cancelOrder(event.getOrderId());
  }

  @EventListener
  public void handleOrderPaid(OrderPaidEvent event) {

    log.info("Order paid: {}", event.getOrderId());
  }

  @EventListener
  public void handleInventoryReserved(InventoryReservedEvent event) {
    if (orderRepository.existsById(event.getOrderId())) {
      log.info("Inventory reserved for order: {}. Completing order.", event.getOrderId());
      orderService.completeOrder(event.getOrderId());
    } else {
      log.warn(
          "Skipping order completion for missing orderId {} after inventory reservation.",
          event.getOrderId());
    }
  }

  @EventListener
  public void handleOrderCompleted(OrderCompletedEvent event) {

    log.info("Order completed: {}", event.getOrderId());
  }

  @EventListener
  public void handleOrderCancelled(OrderCancelledEvent event) {

    log.info("Order cancelled: {}", event.getOrderId());
  }
}
