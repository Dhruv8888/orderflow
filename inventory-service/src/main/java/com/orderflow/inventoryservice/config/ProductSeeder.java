package com.orderflow.inventoryservice.config;

import com.orderflow.inventoryservice.domain.Product;
import com.orderflow.inventoryservice.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ProductSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;

    public ProductSeeder(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }

        productRepository.saveAll(java.util.List.of(
                new Product("SKU-0001", "Wireless Mouse", 50, 0),
                new Product("SKU-0002", "Mechanical Keyboard", 30, 0),
                new Product("SKU-0003", "USB-C Hub", 40, 0),
                new Product("SKU-0004", "27-inch Monitor", 15, 0),
                new Product("SKU-0005", "Webcam 1080p", 25, 0),
                new Product("SKU-0006", "Noise Cancelling Headphones", 20, 0),
                new Product("SKU-0007", "Laptop Stand", 60, 0),
                new Product("SKU-0008", "Desk Lamp", 45, 0),
                new Product("SKU-0009", "Bluetooth Speaker", 35, 0),
                new Product("SKU-0010", "External SSD 1TB", 18, 0),
                new Product("SKU-0011", "Ergonomic Chair", 8, 0),
                new Product("SKU-0012", "Standing Desk Converter", 10, 0),
                new Product("SKU-0013", "Phone Charger Cable", 100, 0),
                new Product("SKU-0014", "Portable Power Bank", 55, 0),
                new Product("SKU-0015", "HDMI Cable 2m", 70, 0),
                new Product("SKU-0016", "Gaming Mouse Pad", 40, 0),
                new Product("SKU-0017", "Wireless Charger Pad", 33, 0),
                new Product("SKU-0018", "4K Action Camera", 12, 0),
                new Product("SKU-0019", "Smart Watch", 22, 0),
                new Product("SKU-0020", "Limited Edition Collector Item", 1, 0)
        ));

        System.out.println("Seeded 20 products into inventory-service.");
    }
}