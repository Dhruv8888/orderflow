package com.orderflow.notificationservice.listener;

import com.orderflow.notificationservice.config.RabbitMQConfig;
import com.orderflow.notificationservice.event.NotificationMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATIONS_QUEUE)
    public void onNotification(NotificationMessage message) {
        System.out.println("[NOTIFICATION] Order " + message.orderId()
                + " - " + message.eventType()
                + " - " + message.details());
    }
}