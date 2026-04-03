package com.example.camt.agreement.value;

/** Reference to the pricing order. Stored and updated per version. */
public record PricingOrderRef(String value) {
    public PricingOrderRef {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("PricingOrderRef must not be blank");
        if (value.length() > 20) throw new IllegalArgumentException("PricingOrderRef must not exceed 20 characters");
    }
}
