package com.orderflow.opsassistant.monitoring;

import java.time.Instant;
import java.util.UUID;

public class FlaggedOrder {

    private final UUID id;
    private final String orderId;
    private final String diagnosis;
    private final Instant detectedAt;

    public FlaggedOrder(String orderId, String diagnosis) {
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.diagnosis = diagnosis;
        this.detectedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getOrderId() { return orderId; }
    public String getDiagnosis() { return diagnosis; }
    public Instant getDetectedAt() { return detectedAt; }
}