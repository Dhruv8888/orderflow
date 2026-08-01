package com.orderflow.paymentservice.event;

import java.util.UUID;

public record PaymentFailedEvent(UUID orderId, String reasonCode) {
}