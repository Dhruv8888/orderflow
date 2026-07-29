package com.orderflow.inventoryservice.event;

import java.util.UUID;

public record StockReservedEvent(UUID orderId, String reservationId) {
}