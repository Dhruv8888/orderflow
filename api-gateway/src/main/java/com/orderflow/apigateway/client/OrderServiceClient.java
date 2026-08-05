package com.orderflow.apigateway.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class OrderServiceClient {

    private final WebClient webClient;

    public OrderServiceClient(WebClient orderServiceWebClient) {
        this.webClient = orderServiceWebClient;
    }

    @CircuitBreaker(name = "orderService", fallbackMethod = "fallback")
    @Retry(name = "orderService")
    public String createOrder(String requestBody) {
        return webClient.post()
                .uri("/orders")
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    @CircuitBreaker(name = "orderService", fallbackMethod = "fallback")
    @Retry(name = "orderService")
    public String getOrder(String orderId) {
        return webClient.get()
                .uri("/orders/{id}", orderId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String fallback(String input, Throwable t) {
        return "{\"error\": \"Order Service is currently unavailable. Please try again shortly.\"}";
    }
}