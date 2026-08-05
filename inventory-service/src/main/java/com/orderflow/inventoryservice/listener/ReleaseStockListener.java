package com.orderflow.inventoryservice.listener;

import com.orderflow.inventoryservice.event.KafkaTopics;
import com.orderflow.inventoryservice.event.ReleaseStockEvent;
import com.orderflow.inventoryservice.event.StockReleasedEvent;
import com.orderflow.inventoryservice.repository.ProcessedReleaseEventRepository;
import com.orderflow.inventoryservice.domain.ProcessedReleaseEvent;
import com.orderflow.inventoryservice.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReleaseStockListener {

    private final InventoryService inventoryService;
    private final ProcessedReleaseEventRepository processedReleaseEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ReleaseStockListener(InventoryService inventoryService,
                                 ProcessedReleaseEventRepository processedReleaseEventRepository,
                                 KafkaTemplate<String, Object> kafkaTemplate) {
        this.inventoryService = inventoryService;
        this.processedReleaseEventRepository = processedReleaseEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = KafkaTopics.RELEASE_STOCK, groupId = "inventory-service", containerFactory = "releaseStockFactory")
    public void onReleaseStock(ReleaseStockEvent event) {
        String orderIdStr = event.orderId().toString();

        if (processedReleaseEventRepository.existsByOrderId(orderIdStr)) {
            System.out.println("Release already processed for order " + orderIdStr + ", skipping (idempotent).");
            kafkaTemplate.send(KafkaTopics.STOCK_RELEASED, orderIdStr, new StockReleasedEvent(event.orderId()));
            return;
        }

        for (ReleaseStockEvent.Item item : event.items()) {
            inventoryService.releaseStock(item.productId(), item.quantity());
        }

        processedReleaseEventRepository.save(new ProcessedReleaseEvent(orderIdStr));

        kafkaTemplate.send(KafkaTopics.STOCK_RELEASED, orderIdStr, new StockReleasedEvent(event.orderId()));
    }
}