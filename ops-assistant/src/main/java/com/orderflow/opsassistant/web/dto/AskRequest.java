package com.orderflow.opsassistant.web.dto;

import jakarta.validation.constraints.NotBlank;

public class AskRequest {

    @NotBlank(message = "orderId is required")
    private String orderId;

    @NotBlank(message = "question is required")
    private String question;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
}