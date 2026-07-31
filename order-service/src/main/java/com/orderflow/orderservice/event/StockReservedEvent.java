package com.orderflow.orderservice.event;

import java.util.UUID;

public record StockReservedEvent(UUID orderId, String reservationId) {
}