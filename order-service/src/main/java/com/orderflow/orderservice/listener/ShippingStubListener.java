package com.orderflow.orderservice.listener;

import com.orderflow.orderservice.event.KafkaTopics;
import com.orderflow.orderservice.event.OrderShippedEvent;
import com.orderflow.orderservice.event.ShipmentFailedEvent;
import com.orderflow.orderservice.event.ShipmentRequestedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class ShippingStubListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final double failureRate;

    public ShippingStubListener(KafkaTemplate<String, Object> kafkaTemplate,
                                 @Value("${shipping.failure-rate:0.0}") double failureRate) {
        this.kafkaTemplate = kafkaTemplate;
        this.failureRate = failureRate;
    }

    @KafkaListener(topics = KafkaTopics.SHIPMENT_REQUESTED, groupId = "order-service", containerFactory = "shipmentRequestedFactory")
    public void onShipmentRequested(ShipmentRequestedEvent event) {
        boolean shouldFail = ThreadLocalRandom.current().nextDouble() < failureRate;

        if (shouldFail) {
            kafkaTemplate.send(KafkaTopics.SHIPMENT_FAILED, event.orderId().toString(),
                    new ShipmentFailedEvent(event.orderId()));
        } else {
            kafkaTemplate.send(KafkaTopics.ORDER_SHIPPED, event.orderId().toString(),
                    new OrderShippedEvent(event.orderId()));
        }
    }
}