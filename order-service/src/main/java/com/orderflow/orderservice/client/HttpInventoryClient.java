package com.orderflow.orderservice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class HttpInventoryClient implements InventoryClient {

    private final WebClient webClient;

    public HttpInventoryClient(WebClient inventoryWebClient) {
        this.webClient = inventoryWebClient;
    }

    @Override
    public ReservationResult reserveStock(String productId, int quantity) {
        try {
            webClient.post()
                    .uri("/products/{id}/reserve", productId)
                    .bodyValue(new ReserveRequestBody(quantity))
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            return new ReservationResult(true, null);

        } catch (WebClientResponseException e) {
            return new ReservationResult(false, "HTTP " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
        }
    }

    private record ReserveRequestBody(int quantity) {
    }
}