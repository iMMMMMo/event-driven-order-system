package com.example.ordersystem.order.stripe;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StripeCheckoutProperties.class)
public class StripeConfiguration {}
