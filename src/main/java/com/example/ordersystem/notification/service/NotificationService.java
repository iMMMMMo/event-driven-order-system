package com.example.ordersystem.notification.service;

import java.util.UUID;

public interface NotificationService {

    void sendOrderCompletedNotification(UUID orderId);
}
