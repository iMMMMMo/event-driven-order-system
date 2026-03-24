package com.example.ordersystem.payment.stripe;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StripeWebhookProperties.class)
public class StripeWebhookConfiguration {}
