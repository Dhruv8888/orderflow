package com.orderflow.opsassistant.remediation;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory store for PendingActions, same rationale as FlaggedOrderStore (7.10):
 * this doesn't need a real database for a portfolio project, and CopyOnWriteArrayList
 * is safe under the @Scheduled monitoring thread writing while a REST request thread reads.
 *
 * Dedup rule: don't let the scheduled monitor create a second PENDING proposal for an
 * order that already has one outstanding. Once a human approves/rejects it (status changes
 * away from PENDING), a future scheduled run is free to propose again if the order is
 * still flagged as stuck.
 */
@Component
public class PendingActionStore {

    private final List<PendingAction> pendingActions = new CopyOnWriteArrayList<>();

    public void add(PendingAction pendingAction) {
        pendingActions.add(pendingAction);
    }

    public List<PendingAction> findAll() {
        return List.copyOf(pendingActions);
    }

    public List<PendingAction> findByStatus(PendingActionStatus status) {
        return pendingActions.stream()
                .filter(a -> a.getStatus() == status)
                .toList();
    }

    public Optional<PendingAction> findById(String id) {
        return pendingActions.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst();
    }

    public boolean hasOutstandingProposal(String orderId) {
        return pendingActions.stream()
                .anyMatch(a -> a.getOrderId().equals(orderId) && a.getStatus() == PendingActionStatus.PENDING);
    }
}