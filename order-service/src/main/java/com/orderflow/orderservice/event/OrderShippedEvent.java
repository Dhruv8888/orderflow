package com.orderflow.orderservice.event;

import java.util.UUID;

public record OrderShippedEvent(UUID orderId) {
}