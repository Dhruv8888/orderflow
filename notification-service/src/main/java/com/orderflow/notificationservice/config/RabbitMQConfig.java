package com.orderflow.notificationservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String NOTIFICATIONS_QUEUE = "notifications";

    @Bean
    public Queue notificationsQueue() {
        return new Queue(NOTIFICATIONS_QUEUE, true);
    }

    @Bean
    public org.springframework.amqp.support.converter.Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
    return new org.springframework.amqp.support.converter.Jackson2JsonMessageConverter();
}
}