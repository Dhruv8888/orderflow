package com.orderflow.opsassistant.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

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

    public String getStuckOrders(int thresholdMinutes) {
        return orderServiceWebClient.get()
                .uri("/orders/stuck?thresholdMinutes={t}", thresholdMinutes)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    /**
     * Calls order-service's internal remediation endpoint (7.16). Only ever invoked
     * after a human has approved a PendingAction — this client method has no opinion
     * on that, it just executes exactly the action string it's given, which is always
     * one of RemediationAction's whitelisted constants by construction.
     */
    public void executeRemediation(String orderId, String action) {
        orderServiceWebClient.post()
                .uri("/orders/{id}/remediate", orderId)
                .bodyValue(Map.of("action", action))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
}