package com.ceylabs.fintrackerbackend.enums;

public enum OverdraftBalanceType {
    ACTUAL_BALANCE,      // Actual balance (can be negative)
    AVAILABLE_BALANCE    // Balance including overdraft limit
}
