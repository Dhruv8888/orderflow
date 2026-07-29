package com.orderflow.inventoryservice.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        String customerId,
        List<Item> items
) {
    public record Item(String productId, int quantity, BigDecimal unitPrice) {
    }
}