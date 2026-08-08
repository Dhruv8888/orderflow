package com.orderflow.opsassistant.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class CoreServicesClient {

    private final WebClient orderServiceWebClient;
    private final WebClient paymentServiceWebClient;

    public CoreServicesClient(@Qualifier("orderServiceWebClient") WebClient orderServiceWebClient,
                               @Qualifier("paymentServiceWebClient") WebClient paymentServiceWebClient) {
        this.orderServiceWebClient = orderServiceWebClient;
        this.paymentServiceWebClient = paymentServiceWebClient;
    }

    public String getOrderStatus(String orderId) {
        return orderServiceWebClient.get()
                .uri("/orders/{id}", orderId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String getOrderHistory(String orderId) {
        return orderServiceWebClient.get()
                .uri("/orders/{id}/history", orderId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String getPaymentDetails(String paymentId) {
        return paymentServiceWebClient.get()
                .uri("/payments/{id}", paymentId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}