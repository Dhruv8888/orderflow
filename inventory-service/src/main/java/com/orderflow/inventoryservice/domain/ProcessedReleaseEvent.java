package com.orderflow.inventoryservice.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "processed_release_events")
public class ProcessedReleaseEvent {

    @Id
    private String id;

    @Indexed(unique = true)
    private String orderId;

    private Instant processedAt;

    public ProcessedReleaseEvent() {
    }

    public ProcessedReleaseEvent(String orderId) {
        this.orderId = orderId;
        this.processedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}