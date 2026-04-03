package com.example.camt.agreement.enums;

/**
 * Lifecycle status of an AgreementScope.
 */
public enum AgreementScopeStatus {

    /**
     * Scope is on a DRAFT or DRAFT_CANCEL version.
     * Transitions to ACTIVE or CANCELLED on parent version approval.
     */
    DRAFT,

    /** Set when parent version is approved as ACTIVE. Live and receiving reports. */
    ACTIVE,

    /**
     * Set immediately at edit time when a scope is removed or replaced,
     * or on approval when the parent version status becomes CANCELLED.
     */
    CANCELLED
}
