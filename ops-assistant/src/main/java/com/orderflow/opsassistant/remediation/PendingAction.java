package com.orderflow.opsassistant.remediation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * A single remediation the agent has proposed for a stuck order, awaiting
 * (or having received) human sign-off.
 *
 * id: server-generated, independent of orderId, because a single order could
 *     in principle accumulate more than one PendingAction over time (e.g. a
 *     rejected proposal followed later by a fresh one after a re-flag).
 * proposedAction: one of the whitelisted RemediationAction values the agent chose.
 * reasoning: the agent's own explanation, straight from the proposeRemediation
 *     tool call arguments — shown to the human reviewer so approval isn't a blind click.
 * status: starts PENDING; transitions to APPROVED/REJECTED via the review
 *     endpoints (7.15), then APPROVED -> EXECUTED once the action actually runs (7.16).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PendingAction {
    private String id;
    private String orderId;
    private RemediationAction proposedAction;
    private String reasoning;
    private PendingActionStatus status;
    private Instant createdAt;

    public static PendingAction newProposal(String orderId, RemediationAction action, String reasoning) {
        PendingAction pendingAction = new PendingAction();
        pendingAction.setId(UUID.randomUUID().toString());
        pendingAction.setOrderId(orderId);
        pendingAction.setProposedAction(action);
        pendingAction.setReasoning(reasoning);
        pendingAction.setStatus(PendingActionStatus.PENDING);
        pendingAction.setCreatedAt(Instant.now());
        return pendingAction;
    }
}