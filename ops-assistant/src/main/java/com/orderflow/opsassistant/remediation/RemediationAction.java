package com.orderflow.opsassistant.remediation;

/**
 * The fixed, whitelisted set of remediation actions the Ops Assistant agent
 * is allowed to propose for a stuck order. This enum is mirrored exactly in
 * the JSON schema "enum" constraint on the proposeRemediation tool definition
 * (see ToolDefinitions.java) — the model cannot propose anything outside this set.
 *
 * NONE is a deliberate first-class value: it lets the agent conclude "I investigated,
 * no safe automated action applies here" instead of being forced to pick a
 * plausible-but-wrong action just because the schema demands one.
 */
public enum RemediationAction {
    REPUBLISH_SHIPMENT_REQUESTED,
    RELEASE_STOCK,
    REFUND_PAYMENT,
    NONE
}