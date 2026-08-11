package com.orderflow.opsassistant.monitoring;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class FlaggedOrderStore {

    private final List<FlaggedOrder> flaggedOrders = new CopyOnWriteArrayList<>();

    public void add(FlaggedOrder flaggedOrder) {
        flaggedOrders.add(flaggedOrder);
    }

    public boolean isAlreadyFlagged(String orderId) {
        return flaggedOrders.stream().anyMatch(f -> f.getOrderId().equals(orderId));
    }

    public List<FlaggedOrder> getAll() {
        return List.copyOf(flaggedOrders);
    }
}