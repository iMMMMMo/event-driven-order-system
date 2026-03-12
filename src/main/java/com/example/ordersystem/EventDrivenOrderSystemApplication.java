package com.example.ordersystem;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EventDrivenOrderSystemApplication {

  public static void main(String[] args) {
    Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    System.setProperty("SECRET_KEY", dotenv.get("SECRET_KEY"));
    System.setProperty("DB_NAME", dotenv.get("DB_NAME"));
    System.setProperty("DB_USER", dotenv.get("DB_USER"));
    System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
    System.setProperty("DB_PORT", dotenv.get("DB_PORT"));

    SpringApplication.run(EventDrivenOrderSystemApplication.class, args);
  }
}
