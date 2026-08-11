package com.orderflow.opsassistant.monitoring;

import com.orderflow.opsassistant.agent.OpsAssistantAgent;
import com.orderflow.opsassistant.client.CoreServicesClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class StuckOrderMonitor {

    private static final int THRESHOLD_MINUTES = 15;

    private final CoreServicesClient coreServicesClient;
    private final OpsAssistantAgent agent;
    private final FlaggedOrderStore store;
    private final ObjectMapper objectMapper;

    public StuckOrderMonitor(CoreServicesClient coreServicesClient,
                              OpsAssistantAgent agent,
                              FlaggedOrderStore store,
                              ObjectMapper objectMapper) {
        this.coreServicesClient = coreServicesClient;
        this.agent = agent;
        this.store = store;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedRate = 300_000)
    public void checkForStuckOrders() {
        String stuckOrdersJson = coreServicesClient.getStuckOrders(THRESHOLD_MINUTES);
        JsonNode stuckOrders = objectMapper.readTree(stuckOrdersJson);

        for (JsonNode order : stuckOrders) {
            String orderId = order.get("orderId").asText();

            if (store.isAlreadyFlagged(orderId)) {
                continue;
            }

            String question = "Regarding order ID " + orderId +
                    ": this order has not progressed in over " + THRESHOLD_MINUTES +
                    " minutes and appears stuck. Diagnose what state it's in and what most likely went wrong, " +
                    "based on its event history.";

            String diagnosis = agent.ask(question);
            store.add(new FlaggedOrder(orderId, diagnosis));
        }
    }
}