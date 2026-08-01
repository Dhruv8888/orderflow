package com.orderflow.orderservice.notification;

import java.util.UUID;

public record NotificationMessage(UUID orderId, String eventType, String details) {
}