package com.orderflow.opsassistant.monitoring;

import com.orderflow.opsassistant.agent.OpsAssistantAgent;
import com.orderflow.opsassistant.client.CoreServicesClient;
import com.orderflow.opsassistant.remediation.PendingAction;
import com.orderflow.opsassistant.remediation.PendingActionStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class StuckOrderMonitor {

    private final int thresholdMinutes;
    private final int maxOrdersPerRun;
    private final long delayBetweenOrdersMs;
    private final CoreServicesClient coreServicesClient;
    private final OpsAssistantAgent agent;
    private final FlaggedOrderStore store;
    private final PendingActionStore pendingActionStore;
    private final ObjectMapper objectMapper;

    public StuckOrderMonitor(CoreServicesClient coreServicesClient,
                              OpsAssistantAgent agent,
                              FlaggedOrderStore store,
                              PendingActionStore pendingActionStore,
                              ObjectMapper objectMapper,
                              @Value("${ops.monitor.threshold-minutes:15}") int thresholdMinutes,
                              @Value("${ops.monitor.max-orders-per-run:5}") int maxOrdersPerRun,
                              @Value("${ops.monitor.delay-between-orders-ms:4000}") long delayBetweenOrdersMs) {
        this.coreServicesClient = coreServicesClient;
        this.agent = agent;
        this.store = store;
        this.pendingActionStore = pendingActionStore;
        this.objectMapper = objectMapper;
        this.thresholdMinutes = thresholdMinutes;
        this.maxOrdersPerRun = maxOrdersPerRun;
        this.delayBetweenOrdersMs = delayBetweenOrdersMs;
    }

    /**
     * Two throttles guard against Gemini's free-tier rate limit (20 req/min):
     * maxOrdersPerRun caps how many orders one scheduled tick will attempt (a large
     * backlog spreads across multiple ticks instead of burning the whole quota at once),
     * and delayBetweenOrdersMs spaces out the ~4 Gemini calls each order costs (diagnosis
     * + remediation, each potentially multi-turn). Defaults (5 orders, 4s apart) are
     * conservative for the free tier; both are irrelevant once/if this moves to a paid tier.
     */
    @Scheduled(fixedRateString = "${ops.monitor.fixed-rate-ms:300000}")
    public void checkForStuckOrders() {
        String stuckOrdersJson = coreServicesClient.getStuckOrders(thresholdMinutes);
        JsonNode stuckOrders = objectMapper.readTree(stuckOrdersJson);

        int processedThisRun = 0;
        for (JsonNode order : stuckOrders) {
            if (processedThisRun >= maxOrdersPerRun) {
                break;
            }

            String orderId = order.get("orderId").asText();

            if (store.isAlreadyFlagged(orderId)) {
                continue;
            }

            try {
                processOneStuckOrder(orderId);
            } catch (Exception e) {
                System.err.println("Failed to process stuck order " + orderId + ": " + e.getMessage());
            }

            processedThisRun++;

            if (processedThisRun < maxOrdersPerRun) {
                sleepQuietly(delayBetweenOrdersMs);
            }
        }
    }

    private void processOneStuckOrder(String orderId) {
        String diagnosisQuestion = "Regarding order ID " + orderId +
                ": this order has not progressed in over " + thresholdMinutes +
                " minutes and appears stuck. Diagnose what state it's in and what most likely went wrong," +
                "based on its event history.";

        String diagnosis = agent.ask(diagnosisQuestion);
        store.add(new FlaggedOrder(orderId, diagnosis));

        proposeRemediationIfNeeded(orderId);
    }

    private void proposeRemediationIfNeeded(String orderId) {
        if (pendingActionStore.hasOutstandingProposal(orderId)) {
            return;
        }

        String remediationQuestion = "Regarding order ID " + orderId +
                ": investigate this stuck order using the available tools, then call " +
                "proposeRemediation exactly once with the single best whitelisted action " +
                "(REPUBLISH_SHIPMENT_REQUESTED, RELEASE_STOCK, REFUND_PAYMENT, or NONE if no " +
                "safe automated action applies) and your reasoning for that choice.";

        OpsAssistantAgent.RemediationProposal proposal =
                agent.diagnoseAndProposeRemediation(remediationQuestion);

        pendingActionStore.add(PendingAction.newProposal(orderId, proposal.action(), proposal.reasoning()));
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}