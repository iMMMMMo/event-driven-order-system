package com.example.ordersystem;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class EventDrivenOrderSystemApplication {

  public static void main(String[] args) {
    Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    require(dotenv, "SECRET_KEY");
    require(dotenv, "DB_NAME");
    require(dotenv, "DB_USER");
    require(dotenv, "DB_PASSWORD");
    require(dotenv, "DB_PORT");

    optional(dotenv, "STRIPE_SECRET_KEY");
    optional(dotenv, "STRIPE_WEBHOOK_SECRET");
    optional(dotenv, "STRIPE_CURRENCY");
    optional(dotenv, "STRIPE_SUCCESS_URL");
    optional(dotenv, "STRIPE_CANCEL_URL");

    SpringApplication.run(EventDrivenOrderSystemApplication.class, args);
  }

  private static void require(Dotenv dotenv, String key) {
    String value = dotenv.get(key);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException("Missing required env variable: " + key);
    }
    System.setProperty(key, value);
  }

  private static void optional(Dotenv dotenv, String key) {
    String value = dotenv.get(key);
    if (value != null && !value.isBlank()) {
      System.setProperty(key, value);
    }
  }
}
