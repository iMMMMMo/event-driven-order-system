package com.example.ordersystem.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ordersystem.config.AbstractIntegrationTest;
import com.example.ordersystem.notification.service.NotificationService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
public class NotificationIntegrationTest extends AbstractIntegrationTest {

  @Autowired private NotificationService notificationService;

  @Test
  void shouldLogOrderCompletedNotification(CapturedOutput output) {
    UUID orderId = UUID.randomUUID();

    notificationService.sendOrderCompletedNotification(orderId);

    assertThat(output.getOut()).contains("Sending notification for completed order " + orderId);
  }
}
