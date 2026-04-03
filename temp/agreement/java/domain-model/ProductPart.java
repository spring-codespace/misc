package com.example.camt.agreement.enums;

/**
 * Identifies which CAMT report type a scope or report config covers.
 */
public enum ProductPart {

    CAMT052_BALANCES_ONLY,
    CAMT052_BALANCES_TX,
    CAMT053_STANDARD,
    CAMT053_EXTENDED,
    CAMT054_DEBIT,

    /**
     * Requires at least one PaymentTypeAssignment.
     * Supports both AccountAssignment and AliasAssignment.
     */
    CAMT054_CREDIT;

    /**
     * Returns true if this product part requires PaymentTypeAssignment rows
     * and may have AliasAssignments.
     */
    public boolean requiresPaymentTypes() {
        return this == CAMT054_CREDIT;
    }
}
