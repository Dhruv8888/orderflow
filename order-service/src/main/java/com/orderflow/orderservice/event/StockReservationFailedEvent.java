package com.orderflow.orderservice.event;

import java.util.UUID;

public record StockReservationFailedEvent(UUID orderId, String reason) {
}