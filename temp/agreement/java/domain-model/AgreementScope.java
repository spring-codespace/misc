package com.example.camt.agreement;

import com.example.camt.agreement.enums.AgreementScopeStatus;
import com.example.camt.agreement.enums.ProductPart;
import com.example.camt.agreement.value.MessageRecipientId;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single recipient + product part combination within an {@link AgreementVersion}.
 *
 * <p>Each scope row defines what a specific message recipient receives
 * (which CAMT report type) under a given version.</p>
 *
 * <p>CANCELLED scope rows carry no PaymentTypeAssignment rows —
 * their assignments are readable from the prior version.</p>
 *
 * <p>Uniqueness constraint: (AgreementVersionId, MessageRecipientId, ProductPart).</p>
 */
@Data
@Builder
public class AgreementScope {

    /** Surrogate primary key. Null until persisted. */
    private Long id;

    /** FK to the parent AgreementVersion. */
    private Long agreementVersionId;

    /** The message recipient (SignerID) for this scope. */
    private MessageRecipientId messageRecipientId;

    /** The CAMT product part this scope covers. */
    private ProductPart productPart;

    /** Current lifecycle status of this scope. */
    private AgreementScopeStatus status;

    /** Timestamp when this scope was created. */
    private Instant createdAt;

    /**
     * Set when the parent version is approved as ACTIVE.
     * Null until activation.
     */
    private Instant activatedAt;

    /**
     * Set immediately when a scope is removed or replaced at edit time,
     * or on approval when the parent version becomes CANCELLED.
     * Null if not cancelled.
     */
    private Instant cancelledAt;

    /**
     * Payment type assignments for this scope.
     * For CAMT054_CREDIT: one row per selected payment type.
     * For all other product parts: one default row acting as the account anchor.
     */
    @Builder.Default
    private List<PaymentTypeAssignment> paymentTypeAssignments = new ArrayList<>();

    /** Returns an unmodifiable view of payment type assignments. */
    public List<PaymentTypeAssignment> getPaymentTypeAssignments() {
        return Collections.unmodifiableList(paymentTypeAssignments);
    }

    public void addPaymentTypeAssignment(PaymentTypeAssignment assignment) {
        this.paymentTypeAssignments.add(assignment);
    }

    // -------------------------------------------------------------------------
    // Behaviour
    // -------------------------------------------------------------------------

    /**
     * Activates this scope when its parent version is approved as ACTIVE.
     *
     * @throws IllegalStateException if the scope is not in DRAFT status
     */
    public void activate(Instant now) {
        if (status != AgreementScopeStatus.DRAFT) {
            throw new IllegalStateException(
                "Cannot activate scope — current status is " + status + ", expected DRAFT");
        }
        this.status = AgreementScopeStatus.ACTIVE;
        this.activatedAt = now;
    }

    /**
     * Cancels this scope.
     *
     * <p>Called immediately at edit time when a scope is removed or replaced,
     * or on approval when the parent version becomes CANCELLED.</p>
     *
     * @throws IllegalStateException if the scope is already cancelled
     */
    public void cancel(Instant now) {
        if (status == AgreementScopeStatus.CANCELLED) {
            throw new IllegalStateException("Scope is already CANCELLED");
        }
        this.status = AgreementScopeStatus.CANCELLED;
        this.cancelledAt = now;
    }

    /** Returns true if this scope is in DRAFT status. */
    public boolean isDraft() {
        return status == AgreementScopeStatus.DRAFT;
    }

    /** Returns true if this scope is in ACTIVE status. */
    public boolean isActive() {
        return status == AgreementScopeStatus.ACTIVE;
    }
}
