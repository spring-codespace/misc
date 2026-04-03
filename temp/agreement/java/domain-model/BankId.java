package com.example.camt.agreement.value;

/** Identifies the bank party to the agreement. */
public record BankId(String value) {
    public BankId {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("BankId must not be blank");
        if (value.length() > 5) throw new IllegalArgumentException("BankId must not exceed 5 characters");
    }
}
