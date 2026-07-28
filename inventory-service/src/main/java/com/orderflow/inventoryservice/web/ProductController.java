package com.orderflow.inventoryservice.web;

import com.orderflow.inventoryservice.domain.Product;
import com.orderflow.inventoryservice.service.InventoryService;
import com.orderflow.inventoryservice.web.dto.ProductResponse;
import com.orderflow.inventoryservice.web.dto.ReserveStockRequest;
import com.orderflow.inventoryservice.web.dto.ReserveStockResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final InventoryService inventoryService;

    public ProductController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable String id) {
        Product product = inventoryService.getProduct(id);

        ProductResponse response = new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getStockLevel(),
                product.getReservedStock(),
                product.getAvailableStock()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<ReserveStockResponse> reserveStock(@PathVariable String id,
                                                               @Valid @RequestBody ReserveStockRequest request) {
        Product updated = inventoryService.reserveStock(id, request.getQuantity());

        ReserveStockResponse response = new ReserveStockResponse(
                updated.getId(),
                updated.getReservedStock(),
                updated.getAvailableStock()
        );

        return ResponseEntity.ok(response);
    }
}