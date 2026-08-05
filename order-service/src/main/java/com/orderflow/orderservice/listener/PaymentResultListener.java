package com.orderflow.orderservice.listener;

import com.orderflow.orderservice.domain.OrderItem;
import com.orderflow.orderservice.domain.OrderStatus;
import com.orderflow.orderservice.event.KafkaTopics;
import com.orderflow.orderservice.event.PaymentCompletedEvent;
import com.orderflow.orderservice.event.PaymentFailedEvent;
import com.orderflow.orderservice.event.ReleaseStockEvent;
import com.orderflow.orderservice.event.ShipmentRequestedEvent;
import com.orderflow.orderservice.exception.IllegalOrderStateTransitionException;
import com.orderflow.orderservice.notification.NotificationPublisher;
import com.orderflow.orderservice.repository.OrderItemRepository;
import com.orderflow.orderservice.service.OrderEventService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PaymentResultListener {

    private final OrderEventService orderEventService;
    private final OrderItemRepository orderItemRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final NotificationPublisher notificationPublisher;

    public PaymentResultListener(OrderEventService orderEventService,
                                  OrderItemRepository orderItemRepository,
                                  KafkaTemplate<String, Object> kafkaTemplate,
                                  NotificationPublisher notificationPublisher) {
        this.orderEventService = orderEventService;
        this.orderItemRepository = orderItemRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.notificationPublisher = notificationPublisher;
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_COMPLETED, groupId = "order-service", containerFactory = "paymentCompletedFactory")
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        try {
            Map<String, Object> payload = Map.of("orderId", event.orderId(), "paymentId", event.paymentId());
            orderEventService.recordEvent(event.orderId(), "PaymentCompleted", OrderStatus.PAID, payload);

            kafkaTemplate.send(KafkaTopics.SHIPMENT_REQUESTED, event.orderId().toString(),
                    new ShipmentRequestedEvent(event.orderId()));

            notificationPublisher.notify(event.orderId(), "PaymentCompleted", "Your payment was successful.");
        } catch (IllegalOrderStateTransitionException e) {
            System.out.println("Ignoring duplicate/out-of-order PaymentCompleted event: " + e.getMessage());
        }
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED, groupId = "order-service", containerFactory = "paymentFailedFactory")
    public void onPaymentFailed(PaymentFailedEvent event) {
        try {
            Map<String, Object> payload = Map.of("orderId", event.orderId(), "reasonCode", event.reasonCode());
            orderEventService.recordEvent(event.orderId(), "PaymentFailed", OrderStatus.FAILED, payload);

            List<ReleaseStockEvent.Item> items = orderItemRepository.findByOrderId(event.orderId()).stream()
                    .map(i -> new ReleaseStockEvent.Item(i.getProductId(), i.getQuantity()))
                    .collect(Collectors.toList());

            kafkaTemplate.send(KafkaTopics.RELEASE_STOCK, event.orderId().toString(),
                    new ReleaseStockEvent(event.orderId(), items));

        } catch (IllegalOrderStateTransitionException e) {
            System.out.println("Ignoring duplicate/out-of-order PaymentFailed event: " + e.getMessage());
        }
    }
}