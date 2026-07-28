package com.orderflow.orderservice.client;

public interface InventoryClient {

    ReservationResult reserveStock(String productId, int quantity);

    record ReservationResult(boolean success, String reason) {
    }
}