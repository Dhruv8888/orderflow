package com.orderflow.inventoryservice.exception;

public class LockAcquisitionException extends RuntimeException {

    public LockAcquisitionException(String productId) {
        super("Could not acquire lock for product " + productId + " - too much contention, try again");
    }
}