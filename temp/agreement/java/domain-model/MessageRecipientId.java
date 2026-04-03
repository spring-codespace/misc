package com.example.camt.agreement.value;

/**
 * Identifies a message recipient (SignerID).
 * Fetched from the corporate connect API. Not a FK — non-unique across scopes.
 */
public record MessageRecipientId(String value) {
    public MessageRecipientId {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("MessageRecipientId must not be blank");
        if (value.length() > 20) throw new IllegalArgumentException("MessageRecipientId must not exceed 20 characters");
    }
}
