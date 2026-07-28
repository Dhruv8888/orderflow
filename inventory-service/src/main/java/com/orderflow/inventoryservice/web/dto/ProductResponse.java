package com.orderflow.inventoryservice.web.dto;

public class ProductResponse {

    private final String id;
    private final String sku;
    private final String name;
    private final int stockLevel;
    private final int reservedStock;
    private final int availableStock;

    public ProductResponse(String id, String sku, String name, int stockLevel, int reservedStock, int availableStock) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.stockLevel = stockLevel;
        this.reservedStock = reservedStock;
        this.availableStock = availableStock;
    }

    public String getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public int getStockLevel() {
        return stockLevel;
    }

    public int getReservedStock() {
        return reservedStock;
    }

    public int getAvailableStock() {
        return availableStock;
    }
}