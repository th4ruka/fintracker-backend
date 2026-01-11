package com.ceylabs.fintrackerbackend.enums;

public enum PaymentStatus {
    CLEARED,     // Payment has been cleared/completed
    PENDING,     // Payment is pending/in progress
    RECONCILED,  // Payment has been reconciled with bank statement
    FAILED,      // Payment failed
    CANCELLED,   // Payment was cancelled
    REFUNDED     // Payment was refunded
}
