package com.orderflow.inventoryservice.repository;

import com.orderflow.inventoryservice.domain.ProcessedReleaseEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProcessedReleaseEventRepository extends MongoRepository<ProcessedReleaseEvent, String> {

    boolean existsByOrderId(String orderId);
}