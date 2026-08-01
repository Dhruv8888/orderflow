package com.orderflow.orderservice.event;

import java.util.UUID;

public record PaymentFailedEvent(UUID orderId, String reasonCode) {
}