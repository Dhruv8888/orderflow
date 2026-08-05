package com.orderflow.apigateway.web;

import com.orderflow.apigateway.client.OrderServiceClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class GatewayController {

    private final OrderServiceClient orderServiceClient;

    public GatewayController(OrderServiceClient orderServiceClient) {
        this.orderServiceClient = orderServiceClient;
    }

    @PostMapping(value = "/orders", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createOrder(@RequestBody String body) {
        return ResponseEntity.ok(orderServiceClient.createOrder(body));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<String> getOrder(@PathVariable String id) {
        return ResponseEntity.ok(orderServiceClient.getOrder(id));
    }
}