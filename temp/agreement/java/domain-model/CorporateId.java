package com.example.camt.agreement.value;

/** External corporate customer identifier. */
public record CorporateId(String value) {
    public CorporateId {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("CorporateId must not be blank");
        if (value.length() > 15) throw new IllegalArgumentException("CorporateId must not exceed 15 characters");
    }
}
