package com.orderflow.orderservice.listener;

import com.orderflow.orderservice.domain.Order;
import com.orderflow.orderservice.domain.OrderStatus;
import com.orderflow.orderservice.event.KafkaTopics;
import com.orderflow.orderservice.event.PaymentRequestedEvent;
import com.orderflow.orderservice.event.StockReservationFailedEvent;
import com.orderflow.orderservice.event.StockReservedEvent;
import com.orderflow.orderservice.service.OrderEventService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InventoryResultListener {

    private final OrderEventService orderEventService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryResultListener(OrderEventService orderEventService,
                                    KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderEventService = orderEventService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = KafkaTopics.STOCK_RESERVED, groupId = "order-service", containerFactory = "stockReservedFactory")
    public void onStockReserved(StockReservedEvent event) {
        Map<String, Object> payload = Map.of(
                "orderId", event.orderId(),
                "reservationId", event.reservationId()
        );

        Order order = orderEventService.recordEvent(event.orderId(), "StockReserved", OrderStatus.STOCK_RESERVED, payload);

        String idempotencyKey = "order-" + order.getId();
        PaymentRequestedEvent paymentRequested = new PaymentRequestedEvent(order.getId(), order.getTotalAmount(), idempotencyKey);

        kafkaTemplate.send(KafkaTopics.PAYMENT_REQUESTED, order.getId().toString(), paymentRequested);
    }

    @KafkaListener(topics = KafkaTopics.STOCK_RESERVATION_FAILED, groupId = "order-service", containerFactory = "stockReservationFailedFactory")
    public void onStockReservationFailed(StockReservationFailedEvent event) {
        Map<String, Object> payload = Map.of(
                "orderId", event.orderId(),
                "reason", event.reason()
        );

        orderEventService.recordEvent(event.orderId(), "StockReservationFailed", OrderStatus.FAILED, payload);
    }
}