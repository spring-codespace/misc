package com.example.camt.agreement;

import com.example.camt.agreement.enums.PaymentTypeCode;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Assigns a payment type to an {@link AgreementScope}.
 *
 * <p>For CAMT054_CREDIT scopes: one row per payment type selected by the customer.
 * At least one payment type required.</p>
 *
 * <p>For all other product parts: a single default row is inserted at scope
 * creation time to act as the anchor for {@link AccountAssignment} rows.</p>
 *
 * <p>Each assignment holds one or more {@link AccountAssignment} rows,
 * and for CAMT054_CREDIT, optionally one or more {@link AliasAssignment} rows.</p>
 */
@Data
@Builder
public class PaymentTypeAssignment {

    /** Surrogate primary key. Null until persisted. */
    private Long id;

    /** FK to the parent AgreementScope. */
    private Long agreementScopeId;

    /** The payment type code for this assignment. */
    private PaymentTypeCode paymentType;

    /** Timestamp when this assignment was created. */
    private Instant createdAt;

    /** One or more account assignments. Always populated. */
    @Builder.Default
    private List<AccountAssignment> accountAssignments = new ArrayList<>();

    /**
     * Alias assignments. Only populated for CAMT054_CREDIT.
     * Empty list for all other product parts.
     */
    @Builder.Default
    private List<AliasAssignment> aliasAssignments = new ArrayList<>();

    /** Returns an unmodifiable view of account assignments. */
    public List<AccountAssignment> getAccountAssignments() {
        return Collections.unmodifiableList(accountAssignments);
    }

    /** Returns an unmodifiable view of alias assignments. */
    public List<AliasAssignment> getAliasAssignments() {
        return Collections.unmodifiableList(aliasAssignments);
    }

    public void addAccountAssignment(AccountAssignment assignment) {
        this.accountAssignments.add(assignment);
    }

    public void addAliasAssignment(AliasAssignment alias) {
        this.aliasAssignments.add(alias);
    }
}
