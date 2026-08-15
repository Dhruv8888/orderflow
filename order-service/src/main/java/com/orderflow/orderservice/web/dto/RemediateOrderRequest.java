package com.orderflow.orderservice.web.dto;

import jakarta.validation.constraints.NotBlank;

public class RemediateOrderRequest {

    @NotBlank
    private String action;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}