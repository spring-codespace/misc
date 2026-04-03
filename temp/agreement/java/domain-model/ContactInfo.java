package com.example.camt.agreement.value;

/**
 * Contact details for an agreement contact.
 * Groups name, email, and phone as a single value object.
 */
public record ContactInfo(String name, String email, String phone) {
    public ContactInfo {
        if (name == null || name.isBlank())   throw new IllegalArgumentException("Contact name must not be blank");
        if (email == null || email.isBlank())  throw new IllegalArgumentException("Contact email must not be blank");
        if (phone == null || phone.isBlank())  throw new IllegalArgumentException("Contact phone must not be blank");
        if (name.length()  > 40) throw new IllegalArgumentException("Contact name must not exceed 40 characters");
        if (email.length() > 50) throw new IllegalArgumentException("Contact email must not exceed 50 characters");
        if (phone.length() > 15) throw new IllegalArgumentException("Contact phone must not exceed 15 characters");
    }
}
