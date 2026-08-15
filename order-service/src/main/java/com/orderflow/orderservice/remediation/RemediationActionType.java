package com.orderflow.orderservice.remediation;

/**
 * The set of remediation actions order-service knows how to execute via
 * POST /orders/{id}/remediate. Intentionally duplicated from ops-assistant's
 * RemediationAction enum (same convention as KafkaTopics being duplicated
 * per-service) rather than a shared library. NONE is deliberately excluded
 * here — it's an ops-assistant-only concept meaning "no action was proposed";
 * it never becomes an HTTP call into this service.
 */
public enum RemediationActionType {
    REPUBLISH_SHIPMENT_REQUESTED,
    RELEASE_STOCK,
    REFUND_PAYMENT
}