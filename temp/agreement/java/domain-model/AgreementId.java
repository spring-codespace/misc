package com.example.camt.agreement.value;

/**
 * Stable identifier for an Agreement. One-time generated (e.g. "6xxxxxREPxxx").
 * Shared across all versions of the same agreement.
 */
public record AgreementId(String value) {
    public AgreementId {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("AgreementId must not be blank");
        if (value.length() > 20) throw new IllegalArgumentException("AgreementId must not exceed 20 characters");
    }
}
