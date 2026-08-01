package com.orderflow.paymentservice.event;

import java.util.UUID;

public record PaymentCompletedEvent(UUID orderId, UUID paymentId) {
}