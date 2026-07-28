package com.orderflow.inventoryservice.service;

import com.orderflow.inventoryservice.domain.Product;
import com.orderflow.inventoryservice.exception.InsufficientStockException;
import com.orderflow.inventoryservice.exception.ProductNotFoundException;
import com.orderflow.inventoryservice.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private final ProductRepository productRepository;

    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product getProduct(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    public Product reserveStock(String productId, int quantity) {
        Product product = getProduct(productId);

        int available = product.getAvailableStock();
        if (quantity > available) {
            throw new InsufficientStockException(productId, quantity, available);
        }

        product.setReservedStock(product.getReservedStock() + quantity);
        return productRepository.save(product);
    }
}