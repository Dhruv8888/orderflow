package com.orderflow.opsassistant.web.dto;

public class AskResponse {

    private final String answer;

    public AskResponse(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }
}