package com.orderflow.orderservice.web.dto;

import java.math.BigDecimal;

public class OrderItemResponse {

    private final String productId;
    private final Integer quantity;
    private final BigDecimal unitPrice;

    public OrderItemResponse(String productId, Integer quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
}