package com.example.ordersystem.payment.service;

import com.example.ordersystem.order.service.OrderService;
import com.example.ordersystem.payment.domain.Payment;
import com.example.ordersystem.payment.event.PaymentSucceededEvent;
import com.example.ordersystem.payment.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void processPayment(UUID orderId, String idempotencyKey) {

        if (paymentRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            return;
        }

        Payment payment = Payment.create(orderId, idempotencyKey);

        // payment simulation (always success)
        payment.markSuccess();

        paymentRepository.save(payment);

        orderService.markAsPaid(orderId);

        eventPublisher.publishEvent(
                new PaymentSucceededEvent(orderId)
        );
    }
}