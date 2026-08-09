package com.orderflow.opsassistant.web;

import com.orderflow.opsassistant.agent.OpsAssistantAgent;
import com.orderflow.opsassistant.web.dto.AskRequest;
import com.orderflow.opsassistant.web.dto.AskResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ops-assistant")
public class OpsAssistantController {

    private final OpsAssistantAgent agent;

    public OpsAssistantController(OpsAssistantAgent agent) {
        this.agent = agent;
    }

    @PostMapping("/ask")
    public ResponseEntity<AskResponse> ask(@Valid @RequestBody AskRequest request) {
        String prompt = "Regarding order ID " + request.getOrderId() + ": " + request.getQuestion();
        String answer = agent.ask(prompt);
        return ResponseEntity.ok(new AskResponse(answer));
    }
}