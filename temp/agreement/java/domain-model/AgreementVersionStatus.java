package com.example.camt.agreement.enums;

/**
 * Lifecycle status of an AgreementVersion.
 */
public enum AgreementVersionStatus {

    /** Created on new agreement or edit. Awaiting customer approval in cart. */
    DRAFT,

    /** Approved and live. Exactly one ACTIVE version per agreement at any time. */
    ACTIVE,

    /** Was ACTIVE; replaced when a newer version was approved. */
    SUPERSEDED,

    /** Cancellation initiated. Awaiting customer approval in cart. */
    DRAFT_CANCEL,

    /** Agreement terminated. Set on DRAFT_CANCEL approval. */
    CANCELLED,

    /** Cart item expired before approval. Set by nightly job. */
    EXPIRED
}
