package com.orderflow.orderservice.event;

import java.util.List;
import java.util.UUID;

public record ReleaseStockEvent(UUID orderId, List<Item> items) {
    public record Item(String productId, int quantity) {
    }
}