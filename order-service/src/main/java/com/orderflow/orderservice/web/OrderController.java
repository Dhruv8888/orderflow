package com.orderflow.orderservice.web;

import com.orderflow.orderservice.domain.Order;
import com.orderflow.orderservice.domain.OrderItem;
import com.orderflow.orderservice.domain.OrderStatus;
import com.orderflow.orderservice.event.KafkaTopics;
import com.orderflow.orderservice.event.OrderCreatedEvent;
import com.orderflow.orderservice.event.OrderEventPublisher;
import com.orderflow.orderservice.event.RefundPaymentEvent;
import com.orderflow.orderservice.event.ReleaseStockEvent;
import com.orderflow.orderservice.event.ShipmentRequestedEvent;
import com.orderflow.orderservice.exception.OrderNotFoundException;
import com.orderflow.orderservice.notification.NotificationPublisher;
import com.orderflow.orderservice.remediation.RemediationActionType;
import com.orderflow.orderservice.repository.OrderEventRepository;
import com.orderflow.orderservice.repository.OrderItemRepository;
import com.orderflow.orderservice.repository.OrderRepository;
import com.orderflow.orderservice.service.OrderEventService;
import com.orderflow.orderservice.web.dto.CreateOrderRequest;
import com.orderflow.orderservice.web.dto.CreateOrderResponse;
import com.orderflow.orderservice.web.dto.OrderEventResponse;
import com.orderflow.orderservice.web.dto.OrderItemResponse;
import com.orderflow.orderservice.web.dto.OrderResponse;
import com.orderflow.orderservice.web.dto.OrderSummaryResponse;
import com.orderflow.orderservice.web.dto.RemediateOrderRequest;
import com.orderflow.orderservice.web.dto.StuckOrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderEventRepository orderEventRepository;
    private final OrderEventService orderEventService;
    private final OrderEventPublisher orderEventPublisher;
    private final NotificationPublisher notificationPublisher;

    public OrderController(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            OrderEventRepository orderEventRepository,
                            OrderEventService orderEventService,
                            OrderEventPublisher orderEventPublisher,
                            NotificationPublisher notificationPublisher) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderEventRepository = orderEventRepository;
        this.orderEventService = orderEventService;
        this.orderEventPublisher = orderEventPublisher;
        this.notificationPublisher = notificationPublisher;
    }

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {

        BigDecimal totalAmount = request.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(totalAmount);
        order = orderRepository.save(order);

        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(itemRequest.getProductId());
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(itemRequest.getUnitPrice());
            orderItemRepository.save(item);
        }

        Map<String, Object> createdPayload = new HashMap<>();
        createdPayload.put("orderId", order.getId());
        createdPayload.put("customerId", order.getCustomerId());
        createdPayload.put("items", request.getItems());

        Order savedOrder = orderEventService.recordEvent(order.getId(), "OrderCreated", OrderStatus.CREATED, createdPayload);

        List<OrderCreatedEvent.Item> eventItems = request.getItems().stream()
                .map(i -> new OrderCreatedEvent.Item(i.getProductId(), i.getQuantity(), i.getUnitPrice()))
                .collect(Collectors.toList());

        OrderCreatedEvent event = new OrderCreatedEvent(savedOrder.getId(), savedOrder.getCustomerId(), eventItems);
        orderEventPublisher.publish(KafkaTopics.ORDER_CREATED, savedOrder.getId().toString(), event);
        notificationPublisher.notify(savedOrder.getId(), "OrderCreated", "Your order has been placed.");

        CreateOrderResponse response = new CreateOrderResponse(savedOrder.getId(), savedOrder.getStatus());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 8.7: list endpoint for the dashboard's order table. Not part of the original PRD's
     * per-order API surface â€” added specifically to back the Angular OrderService.getOrders().
     * Returns lightweight summaries (no items) to avoid an N+1 query against OrderItemRepository
     * for a screen that only renders id/customer/status/createdAt.
     */
    @GetMapping
    public ResponseEntity<List<OrderSummaryResponse>> getOrders() {
        List<OrderSummaryResponse> summaries = orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(o -> new OrderSummaryResponse(
                        o.getId(),
                        o.getCustomerId(),
                        o.getStatus(),
                        o.getTotalAmount(),
                        o.getCreatedAt()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/stuck")
    public ResponseEntity<List<StuckOrderResponse>> getStuckOrders(
            @RequestParam(defaultValue = "15") int thresholdMinutes) {

        List<StuckOrderResponse> stuck = orderEventService.getStuckOrders(thresholdMinutes).stream()
                .map(o -> new StuckOrderResponse(o.getId(), o.getStatus(), o.getUpdatedAt()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(stuck);
    }

    /**
     * Internal endpoint for the Ops Assistant (7.16): re-fires one of the saga's own
     * compensation/progression events for a stuck order, after a human has approved
     * the agent's proposal. Deliberately reuses the exact same OrderEventPublisher +
     * KafkaTopics + event records the saga itself uses in 3C/4A â€” this is not a parallel
     * code path, it's re-entering the normal flow at the point it stalled.
     *
     * Uses recordInformationalEvent (not recordEvent) for the audit log entry: this
     * endpoint does not change order.status directly. The actual transition still only
     * happens when the appropriate listener consumes the republished event, same as
     * it would have on the very first attempt â€” the state machine invariant is preserved.
     */
    @PostMapping("/{id}/remediate")
    public ResponseEntity<Void> remediateOrder(@PathVariable UUID id, @Valid @RequestBody RemediateOrderRequest request) {
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException(id);
        }

        RemediationActionType action;
        try {
            action = RemediationActionType.valueOf(request.getAction());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown remediation action: " + request.getAction());
        }

        Map<String, Object> auditPayload = Map.of("orderId", id, "triggeredBy", "ops-assistant");

        switch (action) {
            case REPUBLISH_SHIPMENT_REQUESTED -> {
                orderEventPublisher.publish(KafkaTopics.SHIPMENT_REQUESTED, id.toString(), new ShipmentRequestedEvent(id));
                orderEventService.recordInformationalEvent(id, "ShipmentRequestedManualRemediation", auditPayload);
            }
            case RELEASE_STOCK -> {
                List<ReleaseStockEvent.Item> items = orderItemRepository.findByOrderId(id).stream()
                        .map(i -> new ReleaseStockEvent.Item(i.getProductId(), i.getQuantity()))
                        .collect(Collectors.toList());
                orderEventPublisher.publish(KafkaTopics.RELEASE_STOCK, id.toString(), new ReleaseStockEvent(id, items));
                orderEventService.recordInformationalEvent(id, "ReleaseStockManualRemediation", auditPayload);
            }
            case REFUND_PAYMENT -> {
                orderEventPublisher.publish(KafkaTopics.REFUND_PAYMENT, id.toString(), new RefundPaymentEvent(id));
                orderEventService.recordInformationalEvent(id, "RefundPaymentManualRemediation", auditPayload);
            }
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id:[0-9a-fA-F-]{36}}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        List<OrderItemResponse> items = orderItemRepository.findByOrderId(id).stream()
                .map(item -> new OrderItemResponse(item.getProductId(), item.getQuantity(), item.getUnitPrice()))
                .collect(Collectors.toList());

        OrderResponse response = new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<OrderEventResponse>> getOrderHistory(@PathVariable UUID id) {
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException(id);
        }

        List<OrderEventResponse> history = orderEventRepository.findByOrderIdOrderByCreatedAtAsc(id).stream()
                .map(event -> new OrderEventResponse(
                        event.getEventType(),
                        event.getPayloadJson(),
                        event.getCreatedAt()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(history);
    }
}