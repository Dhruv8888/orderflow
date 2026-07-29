package com.orderflow.inventoryservice.event;

import java.util.UUID;

public record StockReservationFailedEvent(UUID orderId, String reason) {
}