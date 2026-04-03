package com.example.camt.agreement;

import com.example.camt.agreement.value.CorporateId;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Represents a pending approval item in the customer's cart.
 *
 * <p>One cart item per {@link AgreementVersion}. A DRAFT and a DRAFT_CANCEL
 * version may coexist in the cart for the same agreement simultaneously.</p>
 *
 * <p>Cart items are accessible for 30 days across browser sessions.
 * If not approved within that window, the nightly expiry job expires them.</p>
 *
 * <p>Use {@link #getState()} to determine current state without null-checking
 * individual timestamp fields.</p>
 */
@Data
@Builder
public class CartItem {

    /** Surrogate primary key. Null until persisted. */
    private Long id;

    /** FK to the AgreementVersion this cart item belongs to. One-to-one. */
    private Long agreementVersionId;

    /** Corporate customer identifier. Consistent with Agreement.CorporateId. */
    private CorporateId corporateId;

    /** When this cart item expires if not approved. Set to now + 30 days at creation. */
    private Instant expiresAt;

    /** Set when the customer approves from cart. Null until approved. */
    private Instant approvedAt;

    /**
     * Set by the nightly job when ExpiresAt is breached without approval.
     * Cascades to AgreementVersion.status = EXPIRED.
     * Null until expired.
     */
    private Instant expiredAt;

    /** Timestamp when this cart item was created. */
    private Instant createdAt;

    // -------------------------------------------------------------------------
    // Behaviour
    // -------------------------------------------------------------------------

    /**
     * Resolves the current state of this cart item as a sealed type,
     * enabling exhaustive pattern matching without null checks.
     *
     * <pre>{@code
     * switch (cartItem.getState()) {
     *     case CartItemState.PendingApproval p -> ...
     *     case CartItemState.Approved a        -> ...
     *     case CartItemState.Expired e         -> ...
     * }
     * }</pre>
     */
    public CartItemState getState() {
        if (approvedAt != null) return new CartItemState.Approved(approvedAt);
        if (expiredAt  != null) return new CartItemState.Expired(expiredAt);
        return new CartItemState.PendingApproval(expiresAt);
    }

    /**
     * Marks this cart item as approved by the customer.
     *
     * @throws IllegalStateException if already approved or expired
     */
    public void approve(Instant now) {
        assertPendingApproval("approve");
        this.approvedAt = now;
    }

    /**
     * Marks this cart item as expired by the nightly job.
     *
     * @throws IllegalStateException if already approved or expired
     */
    public void expire(Instant now) {
        assertPendingApproval("expire");
        this.expiredAt = now;
    }

    /** Returns true if this cart item is still awaiting approval. */
    public boolean isPendingApproval() {
        return approvedAt == null && expiredAt == null;
    }

    private void assertPendingApproval(String operation) {
        if (approvedAt != null) {
            throw new IllegalStateException("Cannot " + operation + " cart item — already approved at " + approvedAt);
        }
        if (expiredAt != null) {
            throw new IllegalStateException("Cannot " + operation + " cart item — already expired at " + expiredAt);
        }
    }
}
