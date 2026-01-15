package com.ceylabs.fintrackerbackend.enums;

public enum PaymentType {
    CASH,           // Physical cash payment
    CARD,           // Debit/Credit card payment
    BANK_TRANSFER,  // Direct bank transfer
    UPI,            // Unified Payments Interface (India)
    CHEQUE,         // Cheque payment
    ONLINE,         // Online payment (generic)
    WALLET,         // Digital wallet (PayPal, Venmo, etc.)
    MOBILE_PAYMENT, // Mobile payment apps
    WIRE_TRANSFER,  // International wire transfer
    OTHER           // Other payment methods
}
