package com.orderflow.orderservice.listener;

import com.orderflow.orderservice.domain.OrderStatus;
import com.orderflow.orderservice.event.KafkaTopics;
import com.orderflow.orderservice.event.PaymentCompletedEvent;
import com.orderflow.orderservice.event.PaymentFailedEvent;
import com.orderflow.orderservice.event.ShipmentRequestedEvent;
import com.orderflow.orderservice.notification.NotificationPublisher;
import com.orderflow.orderservice.service.OrderEventService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentResultListener {

    private final OrderEventService orderEventService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final NotificationPublisher notificationPublisher;

    public PaymentResultListener(OrderEventService orderEventService,
                                  KafkaTemplate<String, Object> kafkaTemplate,
                                  NotificationPublisher notificationPublisher) {
        this.orderEventService = orderEventService;
        this.kafkaTemplate = kafkaTemplate;
        this.notificationPublisher = notificationPublisher;
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_COMPLETED, groupId = "order-service", containerFactory = "paymentCompletedFactory")
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        Map<String, Object> payload = Map.of("orderId", event.orderId(), "paymentId", event.paymentId());
        orderEventService.recordEvent(event.orderId(), "PaymentCompleted", OrderStatus.PAID, payload);

        kafkaTemplate.send(KafkaTopics.SHIPMENT_REQUESTED, event.orderId().toString(),
                new ShipmentRequestedEvent(event.orderId()));

        notificationPublisher.notify(event.orderId(), "PaymentCompleted", "Your payment was successful.");
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED, groupId = "order-service", containerFactory = "paymentFailedFactory")
    public void onPaymentFailed(PaymentFailedEvent event) {
        Map<String, Object> payload = Map.of("orderId", event.orderId(), "reasonCode", event.reasonCode());
        orderEventService.recordEvent(event.orderId(), "PaymentFailed", OrderStatus.FAILED, payload);
    }
}