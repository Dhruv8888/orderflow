package com.orderflow.orderservice.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequestedEvent(UUID orderId, BigDecimal amount, String idempotencyKey) {
}