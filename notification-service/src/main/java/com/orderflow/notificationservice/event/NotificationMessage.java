package com.orderflow.notificationservice.event;

import java.util.UUID;

public record NotificationMessage(UUID orderId, String eventType, String details) {
}