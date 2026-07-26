package com.orderflow.orderservice.web.dto;

import java.time.Instant;

public class OrderEventResponse {

    private final String eventType;
    private final String payload;
    private final Instant createdAt;

    public OrderEventResponse(String eventType, String payload, Instant createdAt) {
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = createdAt;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}