package com.orderflow.opsassistant.web;

import com.orderflow.opsassistant.agent.OpsAssistantAgent;
import com.orderflow.opsassistant.client.CoreServicesClient;
import com.orderflow.opsassistant.monitoring.FlaggedOrder;
import com.orderflow.opsassistant.monitoring.FlaggedOrderStore;
import com.orderflow.opsassistant.remediation.PendingAction;
import com.orderflow.opsassistant.remediation.PendingActionStatus;
import com.orderflow.opsassistant.remediation.PendingActionStore;
import com.orderflow.opsassistant.remediation.RemediationAction;
import com.orderflow.opsassistant.web.dto.AskRequest;
import com.orderflow.opsassistant.web.dto.AskResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/ops-assistant")
public class OpsAssistantController {

    private final OpsAssistantAgent agent;
    private final FlaggedOrderStore flaggedOrderStore;
    private final PendingActionStore pendingActionStore;
    private final CoreServicesClient coreServicesClient;

    public OpsAssistantController(OpsAssistantAgent agent,
                                   FlaggedOrderStore flaggedOrderStore,
                                   PendingActionStore pendingActionStore,
                                   CoreServicesClient coreServicesClient) {
        this.agent = agent;
        this.flaggedOrderStore = flaggedOrderStore;
        this.pendingActionStore = pendingActionStore;
        this.coreServicesClient = coreServicesClient;
    }

    @PostMapping("/ask")
    public ResponseEntity<AskResponse> ask(@Valid @RequestBody AskRequest request) {
        String prompt = "Regarding order ID " + request.getOrderId() + ": " + request.getQuestion();
        String answer = agent.ask(prompt);
        return ResponseEntity.ok(new AskResponse(answer));
    }

    @GetMapping("/flagged-orders")
    public ResponseEntity<List<FlaggedOrder>> getFlaggedOrders() {
        return ResponseEntity.ok(flaggedOrderStore.getAll());
    }

    @GetMapping("/pending-actions")
    public ResponseEntity<List<PendingAction>> getPendingActions() {
        return ResponseEntity.ok(pendingActionStore.findByStatus(PendingActionStatus.PENDING));
    }

    /**
     * Approve: mark APPROVED, then actually execute (7.16) by calling order-service's
     * internal remediation endpoint, then mark EXECUTED. A NONE proposal has nothing
     * to execute — it's approved straight to EXECUTED with no outbound call, since NONE
     * means the agent itself concluded no automated action was safe/applicable.
     */
    @PostMapping("/pending-actions/{id}/approve")
    public ResponseEntity<PendingAction> approve(@PathVariable String id) {
        PendingAction pendingAction = requirePendingAction(id);

        pendingAction.setStatus(PendingActionStatus.APPROVED);

        if (pendingAction.getProposedAction() != RemediationAction.NONE) {
            coreServicesClient.executeRemediation(pendingAction.getOrderId(), pendingAction.getProposedAction().name());
        }

        pendingAction.setStatus(PendingActionStatus.EXECUTED);
        return ResponseEntity.ok(pendingAction);
    }

    @PostMapping("/pending-actions/{id}/reject")
    public ResponseEntity<PendingAction> reject(@PathVariable String id) {
        PendingAction pendingAction = requirePendingAction(id);

        pendingAction.setStatus(PendingActionStatus.REJECTED);
        return ResponseEntity.ok(pendingAction);
    }

    private PendingAction requirePendingAction(String id) {
        PendingAction pendingAction = pendingActionStore.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No pending action with id " + id));

        if (pendingAction.getStatus() != PendingActionStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Pending action " + id + " is already " + pendingAction.getStatus());
        }

        return pendingAction;
    }
}