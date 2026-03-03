package com.example.ordersystem.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void sendOrderCompletedNotification(UUID orderId) {
        log.info("Sending notification for completed order {}", orderId);
    }
}