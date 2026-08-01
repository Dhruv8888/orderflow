package com.orderflow.orderservice.listener;

import com.orderflow.orderservice.domain.OrderStatus;
import com.orderflow.orderservice.event.KafkaTopics;
import com.orderflow.orderservice.event.OrderShippedEvent;
import com.orderflow.orderservice.service.OrderEventService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderShippedListener {

    private final OrderEventService orderEventService;

    public OrderShippedListener(OrderEventService orderEventService) {
        this.orderEventService = orderEventService;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_SHIPPED, groupId = "order-service", containerFactory = "orderShippedFactory")
    public void onOrderShipped(OrderShippedEvent event) {
        Map<String, Object> payload = Map.of("orderId", event.orderId());
        orderEventService.recordEvent(event.orderId(), "OrderShipped", OrderStatus.SHIPPED, payload);
    }
}