package com.orderflow.opsassistant.web;

import com.orderflow.opsassistant.agent.OpsAssistantAgent;
import com.orderflow.opsassistant.monitoring.FlaggedOrder;
import com.orderflow.opsassistant.monitoring.FlaggedOrderStore;
import com.orderflow.opsassistant.web.dto.AskRequest;
import com.orderflow.opsassistant.web.dto.AskResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ops-assistant")
public class OpsAssistantController {

    private final OpsAssistantAgent agent;
    private final FlaggedOrderStore flaggedOrderStore;

    public OpsAssistantController(OpsAssistantAgent agent, FlaggedOrderStore flaggedOrderStore) {
        this.agent = agent;
        this.flaggedOrderStore = flaggedOrderStore;
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
}