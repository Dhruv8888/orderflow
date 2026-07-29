package com.orderflow.inventoryservice.listener;

import com.orderflow.inventoryservice.event.KafkaTopics;
import com.orderflow.inventoryservice.event.OrderCreatedEvent;
import com.orderflow.inventoryservice.event.StockReservationFailedEvent;
import com.orderflow.inventoryservice.event.StockReservedEvent;
import com.orderflow.inventoryservice.exception.InsufficientStockException;
import com.orderflow.inventoryservice.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedListener {

    private final InventoryService inventoryService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderCreatedListener(InventoryService inventoryService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.inventoryService = inventoryService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "inventory-service")
    public void onOrderCreated(OrderCreatedEvent event) {
        String orderIdStr = event.orderId().toString();

        try {
            for (OrderCreatedEvent.Item item : event.items()) {
                inventoryService.reserveStock(item.productId(), item.quantity());
            }

            StockReservedEvent reserved = new StockReservedEvent(event.orderId(), orderIdStr);
            kafkaTemplate.send(KafkaTopics.STOCK_RESERVED, orderIdStr, reserved);

        } catch (InsufficientStockException e) {
            StockReservationFailedEvent failed = new StockReservationFailedEvent(event.orderId(), e.getMessage());
            kafkaTemplate.send(KafkaTopics.STOCK_RESERVATION_FAILED, orderIdStr, failed);
        }
    }
}