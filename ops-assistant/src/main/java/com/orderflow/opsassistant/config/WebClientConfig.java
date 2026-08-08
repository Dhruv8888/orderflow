package com.orderflow.opsassistant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient orderServiceWebClient(@Value("${order-service.base-url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient paymentServiceWebClient(@Value("${payment-service.base-url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }
}