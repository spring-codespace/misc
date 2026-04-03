package com.example.camt.agreement;

import com.example.camt.agreement.enums.AgreementVersionStatus;
import com.example.camt.agreement.value.AgreementId;
import com.example.camt.agreement.value.BankId;
import com.example.camt.agreement.value.ContactInfo;
import com.example.camt.agreement.value.CorporateId;
import com.example.camt.agreement.value.PricingOrderRef;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Root aggregate for a corporate reporting agreement.
 *
 * <p>The Agreement is the stable anchor record. Its identity ({@link AgreementId})
 * and name are frozen at creation and never change across versions.</p>
 *
 * <p>Contacts belong directly to the Agreement — they are NOT versioned.
 * Version history is tracked via {@link AgreementVersion} rows.</p>
 *
 * <p>Workflow entry points:
 * <ul>
 *   <li>{@link #updateContacts}    — Edit 3a: contact-only edit, no new version</li>
 *   <li>{@link #submitVersionEdit} — Edit 3b: creates a new DRAFT version</li>
 *   <li>{@link #submitCancellation}— Cancel 4.1: creates a new DRAFT_CANCEL version</li>
 *   <li>{@link #approveActiveVersion}   — Approvals for DRAFT versions (2.2, 3b.2)</li>
 *   <li>{@link #approveCancelVersion}   — Approval for DRAFT_CANCEL versions (4.2)</li>
 * </ul>
 * </p>
 */
@Data
@Builder
public class Agreement {

    /** Stable identifier. One-time generated (e.g. "6xxxxxREPxxx"). Never changes. */
    private AgreementId id;

    /**
     * Agreement name. Frozen at creation — never updated across versions.
     * Max 35 characters.
     */
    private String name;

    /** The bank party to this agreement. */
    private BankId bankId;

    /** External corporate customer identifier. */
    private CorporateId corporateId;

    /** Channel through which the agreement was established (e.g. INTERNET_BANK). */
    private String channel;

    /** Timestamp when this agreement was created. */
    private Instant createdAt;

    /**
     * Contacts for this agreement. Not versioned — always reflect current state.
     * At least one contact required.
     */
    @Builder.Default
    private List<AgreementContact> contacts = new ArrayList<>();

    /**
     * All versions of this agreement, ordered from oldest to newest.
     * Exactly one version will have status ACTIVE at any point in time.
     */
    @Builder.Default
    private List<AgreementVersion> versions = new ArrayList<>();

    /** Returns an unmodifiable view of contacts. */
    public List<AgreementContact> getContacts() {
        return Collections.unmodifiableList(contacts);
    }

    /** Returns an unmodifiable view of versions. */
    public List<AgreementVersion> getVersions() {
        return Collections.unmodifiableList(versions);
    }

    // -------------------------------------------------------------------------
    // Behaviour — Edit 3a: Contact-only edit
    // -------------------------------------------------------------------------

    /**
     * Updates contact information in-place for the given contact ID.
     * No new version is created. No cart or approval flow involved.
     *
     * <p>Corresponds to workflow section 3a.</p>
     *
     * @param contactId  the ID of the contact to update
     * @param updated    the new contact details to apply
     * @throws IllegalArgumentException if no contact with the given ID exists
     */
    public void updateContact(Long contactId, ContactInfo updated) {
        AgreementContact contact = contacts.stream()
                .filter(c -> contactId.equals(c.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No contact found with id " + contactId + " on agreement " + id.value()));
        contact.updateInfo(updated);
    }

    /**
     * Replaces all contacts in-place with the provided list.
     * Used when the customer updates the full contact list at once.
     * No new version is created.
     *
     * <p>Corresponds to workflow section 3a.</p>
     *
     * @param updatedContacts new list of contacts; must not be empty
     * @throws IllegalArgumentException if the list is null or empty
     */
    public void updateContacts(List<AgreementContact> updatedContacts) {
        if (updatedContacts == null || updatedContacts.isEmpty()) {
            throw new IllegalArgumentException("At least one contact is required");
        }
        this.contacts = new ArrayList<>(updatedContacts);
    }

    // -------------------------------------------------------------------------
    // Behaviour — Edit 3b: Version edit
    // -------------------------------------------------------------------------

    /**
     * Creates and registers a new DRAFT version for a version-level edit.
     * The current ACTIVE version continues uninterrupted until the draft is approved.
     *
     * <p>Corresponds to workflow section 3b.1 step 1.</p>
     *
     * @param pricingOrderRef pricing order reference for the new version
     * @param now             current timestamp
     * @return the newly created DRAFT version, ready to have scopes added
     * @throws IllegalStateException if there is no current ACTIVE version
     */
    public AgreementVersion submitVersionEdit(PricingOrderRef pricingOrderRef, Instant now) {
        assertActiveVersionExists();
        AgreementVersion draft = AgreementVersion.builder()
                .agreementId(this.id)
                .status(AgreementVersionStatus.DRAFT)
                .pricingOrderRef(pricingOrderRef)
                .createdAt(now)
                .build();
        versions.add(draft);
        return draft;
    }

    // -------------------------------------------------------------------------
    // Behaviour — Cancel 4.1: Cancellation initiation
    // -------------------------------------------------------------------------

    /**
     * Creates and registers a new DRAFT_CANCEL version to initiate cancellation.
     * The current ACTIVE version continues uninterrupted until the cancellation is approved.
     *
     * <p>Corresponds to workflow section 4.1 step 1.</p>
     *
     * @param pricingOrderRef pricing order reference copied forward from current version
     * @param now             current timestamp
     * @return the newly created DRAFT_CANCEL version, ready to have scopes copied forward
     * @throws IllegalStateException if there is no current ACTIVE version
     */
    public AgreementVersion submitCancellation(PricingOrderRef pricingOrderRef, Instant now) {
        assertActiveVersionExists();
        AgreementVersion draftCancel = AgreementVersion.builder()
                .agreementId(this.id)
                .status(AgreementVersionStatus.DRAFT_CANCEL)
                .pricingOrderRef(pricingOrderRef)
                .createdAt(now)
                .build();
        versions.add(draftCancel);
        return draftCancel;
    }

    // -------------------------------------------------------------------------
    // Behaviour — Approvals (2.2, 3b.2)
    // -------------------------------------------------------------------------

    /**
     * Approves a DRAFT version, transitioning it to ACTIVE.
     * The previously ACTIVE version (if any) is superseded.
     * The cart item for the approved version is also approved.
     *
     * <p>Corresponds to workflow sections 2.2 and 3b.2.</p>
     *
     * @param versionId the ID of the DRAFT version to approve
     * @param cartItem  the cart item associated with this version
     * @param now       current timestamp
     * @throws IllegalArgumentException if no DRAFT version with the given ID exists on this agreement
     */
    public void approveActiveVersion(Long versionId, CartItem cartItem, Instant now) {
        AgreementVersion toActivate = findDraftVersion(versionId, AgreementVersionStatus.DRAFT);

        // Supersede the current ACTIVE version if one exists
        findCurrentActiveVersion().ifPresent(active -> active.supersede(now));

        // Activate the new version (also activates all its DRAFT scopes)
        toActivate.approveAsActive(now);

        // Approve the cart item
        cartItem.approve(now);
    }

    // -------------------------------------------------------------------------
    // Behaviour — Cancel approval (4.2)
    // -------------------------------------------------------------------------

    /**
     * Approves a DRAFT_CANCEL version, transitioning it to CANCELLED.
     * The previously ACTIVE version is superseded.
     * The cart item for the approved version is also approved.
     *
     * <p>Corresponds to workflow section 4.2.</p>
     *
     * @param versionId the ID of the DRAFT_CANCEL version to approve
     * @param cartItem  the cart item associated with this version
     * @param now       current timestamp
     * @throws IllegalArgumentException if no DRAFT_CANCEL version with the given ID exists
     */
    public void approveCancelVersion(Long versionId, CartItem cartItem, Instant now) {
        AgreementVersion toCancel = findDraftVersion(versionId, AgreementVersionStatus.DRAFT_CANCEL);

        // Supersede the current ACTIVE version
        findCurrentActiveVersion().ifPresent(active -> active.supersede(now));

        // Cancel the new version (also cancels all its DRAFT scopes)
        toCancel.approveAsCancelled(now);

        // Approve the cart item
        cartItem.approve(now);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /**
     * Returns the current ACTIVE version, if one exists.
     *
     * @return the ACTIVE version, or empty if none
     */
    public java.util.Optional<AgreementVersion> findCurrentActiveVersion() {
        return versions.stream()
                .filter(AgreementVersion::isActive)
                .findFirst();
    }

    /**
     * Returns true if this agreement has ever been activated
     * (i.e. has at least one non-DRAFT version).
     */
    public boolean hasBeenActivated() {
        return versions.stream().anyMatch(v -> v.getStatus() != AgreementVersionStatus.DRAFT);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void assertActiveVersionExists() {
        if (findCurrentActiveVersion().isEmpty()) {
            throw new IllegalStateException(
                "Cannot create a new version — no ACTIVE version exists for agreement " + id.value());
        }
    }

    private AgreementVersion findDraftVersion(Long versionId, AgreementVersionStatus expectedStatus) {
        return versions.stream()
                .filter(v -> versionId.equals(v.getId()) && v.getStatus() == expectedStatus)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No " + expectedStatus + " version with id " + versionId
                        + " found on agreement " + id.value()));
    }
}
