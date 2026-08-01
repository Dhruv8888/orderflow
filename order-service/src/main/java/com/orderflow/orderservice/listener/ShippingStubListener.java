package com.orderflow.orderservice.listener;

import com.orderflow.orderservice.event.KafkaTopics;
import com.orderflow.orderservice.event.OrderShippedEvent;
import com.orderflow.orderservice.event.ShipmentRequestedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ShippingStubListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ShippingStubListener(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = KafkaTopics.SHIPMENT_REQUESTED, groupId = "order-service", containerFactory = "shipmentRequestedFactory")
    public void onShipmentRequested(ShipmentRequestedEvent event) {
        kafkaTemplate.send(KafkaTopics.ORDER_SHIPPED, event.orderId().toString(),
                new OrderShippedEvent(event.orderId()));
    }
}