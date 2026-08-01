package com.orderflow.orderservice.notification;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NotificationPublisher {

    private static final String NOTIFICATIONS_QUEUE = "notifications";

    private final RabbitTemplate rabbitTemplate;

    public NotificationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void notify(UUID orderId, String eventType, String details) {
        rabbitTemplate.convertAndSend(NOTIFICATIONS_QUEUE, new NotificationMessage(orderId, eventType, details));
    }
}