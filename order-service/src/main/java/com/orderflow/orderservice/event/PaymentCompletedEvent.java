package com.orderflow.orderservice.event;

import java.util.UUID;

public record PaymentCompletedEvent(UUID orderId, UUID paymentId) {
}