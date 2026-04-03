package com.example.camt.agreement;

import java.time.Instant;

/**
 * Represents the current state of a CartItem as a sealed type.
 * Enables exhaustive pattern matching on cart state without
 * null-checking multiple timestamp fields.
 *
 * <ul>
 *   <li>{@link PendingApproval} — created, awaiting customer action</li>
 *   <li>{@link Approved}        — customer approved from cart</li>
 *   <li>{@link Expired}         — nightly job expired it before approval</li>
 * </ul>
 */
public sealed interface CartItemState permits
        CartItemState.PendingApproval,
        CartItemState.Approved,
        CartItemState.Expired {

    /**
     * Cart item is awaiting customer approval.
     *
     * @param expiresAt when the item will expire if not approved
     */
    record PendingApproval(Instant expiresAt) implements CartItemState {}

    /**
     * Customer approved the cart item.
     *
     * @param approvedAt timestamp of approval
     */
    record Approved(Instant approvedAt) implements CartItemState {}

    /**
     * Nightly job expired the cart item before approval was received.
     *
     * @param expiredAt timestamp set by the nightly job
     */
    record Expired(Instant expiredAt) implements CartItemState {}
}
