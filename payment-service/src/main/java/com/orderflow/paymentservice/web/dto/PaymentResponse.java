package com.orderflow.paymentservice.web.dto;

import com.orderflow.paymentservice.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PaymentResponse {

    private final UUID id;
    private final UUID orderId;
    private final PaymentStatus status;
    private final BigDecimal amount;
    private final Instant createdAt;

    public PaymentResponse(UUID id, UUID orderId, PaymentStatus status, BigDecimal amount, Instant createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.status = status;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public PaymentStatus getStatus() { return status; }
    public BigDecimal getAmount() { return amount; }
    public Instant getCreatedAt() { return createdAt; }
}