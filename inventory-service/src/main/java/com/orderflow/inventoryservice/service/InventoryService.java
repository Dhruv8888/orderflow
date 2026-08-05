package com.orderflow.inventoryservice.service;

import com.orderflow.inventoryservice.domain.Product;
import com.orderflow.inventoryservice.exception.InsufficientStockException;
import com.orderflow.inventoryservice.exception.LockAcquisitionException;
import com.orderflow.inventoryservice.exception.ProductNotFoundException;
import com.orderflow.inventoryservice.lock.RedisLockService;
import com.orderflow.inventoryservice.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class InventoryService {

    private static final Duration LOCK_TTL = Duration.ofSeconds(5);
    private static final int MAX_RETRIES = 3;

    private final ProductRepository productRepository;
    private final RedisLockService lockService;

    public InventoryService(ProductRepository productRepository, RedisLockService lockService) {
        this.productRepository = productRepository;
        this.lockService = lockService;
    }

    @Cacheable(value = "products", key = "#productId")
    public Product getProduct(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    @CacheEvict(value = "products", key = "#productId")
    public Product reserveStock(String productId, int quantity) {
        return withLock(productId, () -> doReserve(productId, quantity));
    }

    @CacheEvict(value = "products", key = "#productId")
    public Product releaseStock(String productId, int quantity) {
        return withLock(productId, () -> doRelease(productId, quantity));
    }

    private Product doReserve(String productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        int available = product.getAvailableStock();
        if (quantity > available) {
            throw new InsufficientStockException(productId, quantity, available);
        }

        product.setReservedStock(product.getReservedStock() + quantity);
        return productRepository.save(product);
    }

    private Product doRelease(String productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        int newReserved = Math.max(0, product.getReservedStock() - quantity);
        product.setReservedStock(newReserved);
        return productRepository.save(product);
    }

    private Product withLock(String productId, java.util.function.Supplier<Product> action) {
        String lockKey = "lock:product:" + productId;
        String token = lockService.tryLock(lockKey, LOCK_TTL);

        if (token == null) {
            throw new LockAcquisitionException(productId);
        }

        try {
            int attempts = 0;
            while (true) {
                try {
                    return action.get();
                } catch (OptimisticLockingFailureException e) {
                    attempts++;
                    if (attempts >= MAX_RETRIES) {
                        throw e;
                    }
                }
            }
        } finally {
            lockService.unlock(lockKey, token);
        }
    }
}