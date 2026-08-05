package com.orderflow.orderservice.event;

import java.util.UUID;

public record StockReleasedEvent(UUID orderId) {
}