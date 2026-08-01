package com.orderflow.orderservice.web;

import com.orderflow.orderservice.domain.Order;
import com.orderflow.orderservice.domain.OrderItem;
import com.orderflow.orderservice.domain.OrderStatus;
import com.orderflow.orderservice.event.KafkaTopics;
import com.orderflow.orderservice.event.OrderCreatedEvent;
import com.orderflow.orderservice.event.OrderEventPublisher;
import com.orderflow.orderservice.exception.OrderNotFoundException;
import com.orderflow.orderservice.notification.NotificationPublisher;
import com.orderflow.orderservice.repository.OrderEventRepository;
import com.orderflow.orderservice.repository.OrderItemRepository;
import com.orderflow.orderservice.repository.OrderRepository;
import com.orderflow.orderservice.service.OrderEventService;
import com.orderflow.orderservice.web.dto.CreateOrderRequest;
import com.orderflow.orderservice.web.dto.CreateOrderResponse;
import com.orderflow.orderservice.web.dto.OrderEventResponse;
import com.orderflow.orderservice.web.dto.OrderItemResponse;
import com.orderflow.orderservice.web.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/{id}")
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