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
    private final boolean enabled;

    public ShippingStubListener(KafkaTemplate<String, Object> kafkaTemplate,
                                 @Value("${shipping.failure-rate:0.0}") double failureRate,
                                 @Value("${shipping.stub.enabled:true}") boolean enabled) {
        this.kafkaTemplate = kafkaTemplate;
        this.failureRate = failureRate;
        this.enabled = enabled;
    }

    /**
     * shipping.stub.enabled=false (test-only, via env var SHIPPING_STUB_ENABLED=false)
     * makes this listener consume ShipmentRequested and do nothing — no OrderShipped,
     * no ShipmentFailed. That's how 7.18 manufactures a permanently-stuck PAID order
     * without touching any other code. Default is enabled=true, i.e. normal behavior,
     * so simply not setting the env var (or setting it back to true) restores production
     * behavior with no code changes to revert.
     */
    @KafkaListener(topics = KafkaTopics.SHIPMENT_REQUESTED, groupId = "order-service", containerFactory = "shipmentRequestedFactory")
    public void onShipmentRequested(ShipmentRequestedEvent event) {
        if (!enabled) {
            return;
        }

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