package com.orderflow.orderservice.web.dto;

import com.orderflow.orderservice.domain.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public class StuckOrderResponse {

    private final UUID orderId;
    private final OrderStatus status;
    private final Instant lastUpdatedAt;

    public StuckOrderResponse(UUID orderId, OrderStatus status, Instant lastUpdatedAt) {
        this.orderId = orderId;
        this.status = status;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public UUID getOrderId() { return orderId; }
    public OrderStatus getStatus() { return status; }
    public Instant getLastUpdatedAt() { return lastUpdatedAt; }
}