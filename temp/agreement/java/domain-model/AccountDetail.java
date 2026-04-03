package com.example.camt.agreement.value;

/**
 * Account identification details.
 * Same BBAN/IBAN may appear across multiple scopes for the same product part
 * within a version — no exclusivity constraint.
 */
public record AccountDetail(String bban, String iban, String currency) {
    public AccountDetail {
        if (bban == null || bban.isBlank())         throw new IllegalArgumentException("BBAN must not be blank");
        if (iban == null || iban.isBlank())         throw new IllegalArgumentException("IBAN must not be blank");
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("Currency must not be blank");
        if (bban.length()     > 15) throw new IllegalArgumentException("BBAN must not exceed 15 characters");
        if (iban.length()     > 35) throw new IllegalArgumentException("IBAN must not exceed 35 characters");
        if (currency.length() != 3) throw new IllegalArgumentException("Currency must be exactly 3 characters");
    }
}
