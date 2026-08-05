package com.orderflow.orderservice.listener;

import com.orderflow.orderservice.event.KafkaTopics;
import com.orderflow.orderservice.event.ReleaseStockEvent;
import com.orderflow.orderservice.event.RefundPaymentEvent;
import com.orderflow.orderservice.event.ShipmentFailedEvent;
import com.orderflow.orderservice.repository.OrderItemRepository;
import com.orderflow.orderservice.service.OrderEventService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ShipmentFailedListener {

    private final OrderEventService orderEventService;
    private final OrderItemRepository orderItemRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ShipmentFailedListener(OrderEventService orderEventService,
                                   OrderItemRepository orderItemRepository,
                                   KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderEventService = orderEventService;
        this.orderItemRepository = orderItemRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = KafkaTopics.SHIPMENT_FAILED, groupId = "order-service", containerFactory = "shipmentFailedFactory")
    public void onShipmentFailed(ShipmentFailedEvent event) {
        orderEventService.recordInformationalEvent(event.orderId(), "ShipmentFailed", Map.of("orderId", event.orderId()));

        kafkaTemplate.send(KafkaTopics.REFUND_PAYMENT, event.orderId().toString(),
                new RefundPaymentEvent(event.orderId()));

        List<ReleaseStockEvent.Item> items = orderItemRepository.findByOrderId(event.orderId()).stream()
                .map(i -> new ReleaseStockEvent.Item(i.getProductId(), i.getQuantity()))
                .collect(Collectors.toList());

        kafkaTemplate.send(KafkaTopics.RELEASE_STOCK, event.orderId().toString(),
                new ReleaseStockEvent(event.orderId(), items));
    }
}