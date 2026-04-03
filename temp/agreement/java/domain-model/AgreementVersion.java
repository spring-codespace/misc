package com.example.camt.agreement;

import com.example.camt.agreement.enums.AgreementScopeStatus;
import com.example.camt.agreement.enums.AgreementVersionStatus;
import com.example.camt.agreement.value.AgreementId;
import com.example.camt.agreement.value.PricingOrderRef;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A snapshot of an {@link Agreement} at a point in time.
 *
 * <p>A new version is created on every edit or cancellation initiation.
 * Exactly one ACTIVE version per agreement at any time (enforced by a
 * filtered unique index in the database).</p>
 *
 * <p>Version lifecycle:
 * <pre>
 *   DRAFT ──────────────────► ACTIVE ──► SUPERSEDED
 *     │                                      (on next version approval)
 *     └──► EXPIRED  (nightly job, no approval within 30 days)
 *
 *   DRAFT_CANCEL ───────────► CANCELLED
 *     │
 *     └──► EXPIRED  (nightly job)
 * </pre>
 * </p>
 */
@Data
@Builder
public class AgreementVersion {

    /** Surrogate primary key. Null until persisted. */
    private Long id;

    /** The agreement this version belongs to. */
    private AgreementId agreementId;

    /** Current lifecycle status. */
    private AgreementVersionStatus status;

    /** Pricing order reference for this version. Updated per version. */
    private PricingOrderRef pricingOrderRef;

    /** Timestamp when this version was created. */
    private Instant createdAt;

    /** Set when status transitions to ACTIVE. */
    private Instant activatedAt;

    /** Set when a newer version is approved, replacing this one. */
    private Instant supersededAt;

    /** Set when status transitions to CANCELLED. */
    private Instant cancelledAt;

    /** Set by the nightly job when the cart item expires without approval. */
    private Instant expiredAt;

    /** All scopes belonging to this version. */
    @Builder.Default
    private List<AgreementScope> scopes = new ArrayList<>();

    /** Returns an unmodifiable view of scopes. */
    public List<AgreementScope> getScopes() {
        return Collections.unmodifiableList(scopes);
    }

    public void addScope(AgreementScope scope) {
        this.scopes.add(scope);
    }

    // -------------------------------------------------------------------------
    // Behaviour
    // -------------------------------------------------------------------------

    /**
     * Approves this version, transitioning it to ACTIVE.
     * Activates all DRAFT scopes on this version.
     *
     * <p>Used for new agreement onboarding (workflow 2.2) and
     * version edit approval (workflow 3b.2).</p>
     *
     * @throws IllegalStateException if the version is not in DRAFT status
     */
    public void approveAsActive(Instant now) {
        if (status != AgreementVersionStatus.DRAFT) {
            throw new IllegalStateException(
                "Cannot approve version as ACTIVE — current status is " + status + ", expected DRAFT");
        }
        this.status = AgreementVersionStatus.ACTIVE;
        this.activatedAt = now;
        activateDraftScopes(now);
    }

    /**
     * Approves this version as CANCELLED.
     * Cancels all DRAFT scopes on this version.
     *
     * <p>Used for cancellation approval (workflow 4.2).</p>
     *
     * @throws IllegalStateException if the version is not in DRAFT_CANCEL status
     */
    public void approveAsCancelled(Instant now) {
        if (status != AgreementVersionStatus.DRAFT_CANCEL) {
            throw new IllegalStateException(
                "Cannot approve version as CANCELLED — current status is " + status + ", expected DRAFT_CANCEL");
        }
        this.status = AgreementVersionStatus.CANCELLED;
        this.cancelledAt = now;
        cancelDraftScopes(now);
    }

    /**
     * Supersedes this version when a newer version is approved.
     * The previously ACTIVE version transitions to SUPERSEDED.
     *
     * @throws IllegalStateException if the version is not in ACTIVE status
     */
    public void supersede(Instant now) {
        if (status != AgreementVersionStatus.ACTIVE) {
            throw new IllegalStateException(
                "Cannot supersede version — current status is " + status + ", expected ACTIVE");
        }
        this.status = AgreementVersionStatus.SUPERSEDED;
        this.supersededAt = now;
    }

    /**
     * Expires this version. Called by the nightly job when the cart item
     * expires without approval.
     *
     * @throws IllegalStateException if the version is not in DRAFT or DRAFT_CANCEL status
     */
    public void expire(Instant now) {
        if (status != AgreementVersionStatus.DRAFT && status != AgreementVersionStatus.DRAFT_CANCEL) {
            throw new IllegalStateException(
                "Cannot expire version — current status is " + status + ", expected DRAFT or DRAFT_CANCEL");
        }
        this.status = AgreementVersionStatus.EXPIRED;
        this.expiredAt = now;
    }

    /** Returns true if this version is in DRAFT status. */
    public boolean isDraft() {
        return status == AgreementVersionStatus.DRAFT;
    }

    /** Returns true if this version is in ACTIVE status. */
    public boolean isActive() {
        return status == AgreementVersionStatus.ACTIVE;
    }

    /** Returns true if this version is in DRAFT_CANCEL status. */
    public boolean isDraftCancel() {
        return status == AgreementVersionStatus.DRAFT_CANCEL;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void activateDraftScopes(Instant now) {
        scopes.stream()
              .filter(s -> s.getStatus() == AgreementScopeStatus.DRAFT)
              .forEach(s -> s.activate(now));
    }

    private void cancelDraftScopes(Instant now) {
        scopes.stream()
              .filter(s -> s.getStatus() == AgreementScopeStatus.DRAFT)
              .forEach(s -> s.cancel(now));
    }
}
