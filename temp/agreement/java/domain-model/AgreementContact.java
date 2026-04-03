package com.example.camt.agreement;

import com.example.camt.agreement.value.AgreementId;
import com.example.camt.agreement.value.ContactInfo;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * A contact associated with an Agreement.
 *
 * <p>Contacts are NOT versioned — they belong directly to {@link Agreement}
 * and are updated in-place via {@link Agreement#updateContacts}.
 * At least one contact is required per agreement.</p>
 */
@Data
@Builder
public class AgreementContact {

    /** Surrogate primary key. Null until persisted. */
    private Long id;

    /** The agreement this contact belongs to. */
    private AgreementId agreementId;

    /** Name, email, and phone of the contact. */
    private ContactInfo contactInfo;

    /** Timestamp when this contact was created. */
    private Instant createdAt;

    /**
     * Updates the contact details in-place.
     * This is the only mutation allowed — no versioning involved.
     *
     * @param updated new contact details
     */
    public void updateInfo(ContactInfo updated) {
        this.contactInfo = updated;
    }
}
