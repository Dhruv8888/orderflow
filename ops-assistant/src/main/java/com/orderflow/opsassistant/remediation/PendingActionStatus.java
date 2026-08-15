package com.orderflow.opsassistant.remediation;

/**
 * Lifecycle of a PendingAction, from the moment the agent proposes it (PENDING)
 * through human review (APPROVED/REJECTED) to actual side-effecting execution (EXECUTED).
 *
 * Note EXECUTED is a distinct state from APPROVED: approval is a human decision,
 * execution is a separate Java-code action that happens after approval (7.16).
 * Keeping them distinct means an approved-but-not-yet-executed state is always
 * inspectable, and execution failures don't get silently conflated with rejection.
 */
public enum PendingActionStatus {
    PENDING,
    APPROVED,
    REJECTED,
    EXECUTED
}