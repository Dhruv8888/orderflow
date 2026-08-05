package com.orderflow.orderservice.listener;

import com.orderflow.orderservice.domain.Order;
import com.orderflow.orderservice.domain.OrderStatus;
import com.orderflow.orderservice.event.KafkaTopics;
import com.orderflow.orderservice.event.PaymentRefundedEvent;
import com.orderflow.orderservice.event.StockReleasedEvent;
import com.orderflow.orderservice.exception.OrderNotFoundException;
import com.orderflow.orderservice.repository.OrderRepository;
import com.orderflow.orderservice.service.OrderEventService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
public class CompensationConfirmationListener {

    private final OrderRepository orderRepository;
    private final OrderEventService orderEventService;

    public CompensationConfirmationListener(OrderRepository orderRepository, OrderEventService orderEventService) {
        this.orderRepository = orderRepository;
        this.orderEventService = orderEventService;
    }

    @KafkaListener(topics = KafkaTopics.STOCK_RELEASED, groupId = "order-service", containerFactory = "stockReleasedFactory")
    @Transactional
    public void onStockReleased(StockReleasedEvent event) {
        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new OrderNotFoundException(event.orderId()));

        if (order.isStockReleased()) {
            return;
        }

        order.setStockReleased(true);
        orderRepository.save(order);

        orderEventService.recordInformationalEvent(event.orderId(), "StockReleased", Map.of("orderId", event.orderId()));

        maybeFinalizeCancellation(order);
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_REFUNDED, groupId = "order-service", containerFactory = "paymentRefundedFactory")
    @Transactional
    public void onPaymentRefunded(PaymentRefundedEvent event) {
        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new OrderNotFoundException(event.orderId()));

        if (order.isPaymentRefunded()) {
            return;
        }

        order.setPaymentRefunded(true);
        orderRepository.save(order);

        orderEventService.recordInformationalEvent(event.orderId(), "PaymentRefunded", Map.of("orderId", event.orderId()));

        maybeFinalizeCancellation(order);
    }

    private void maybeFinalizeCancellation(Order order) {
        if (order.isStockReleased() && order.isPaymentRefunded()) {
            orderEventService.recordEvent(order.getId(), "OrderCancelled", OrderStatus.CANCELLED,
                    Map.of("orderId", order.getId()));
        }
    }
}