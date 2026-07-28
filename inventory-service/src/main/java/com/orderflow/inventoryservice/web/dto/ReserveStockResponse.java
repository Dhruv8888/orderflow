package com.orderflow.inventoryservice.web.dto;

public class ReserveStockResponse {

    private final String productId;
    private final int reservedStock;
    private final int availableStock;

    public ReserveStockResponse(String productId, int reservedStock, int availableStock) {
        this.productId = productId;
        this.reservedStock = reservedStock;
        this.availableStock = availableStock;
    }

    public String getProductId() {
        return productId;
    }

    public int getReservedStock() {
        return reservedStock;
    }

    public int getAvailableStock() {
        return availableStock;
    }
}