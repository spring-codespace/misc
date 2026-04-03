package com.example.camt.agreement;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * An alias assigned to a {@link PaymentTypeAssignment}.
 *
 * <p>Only applicable for CAMT054_CREDIT product parts.
 * One or more aliases may be assigned per payment type assignment.</p>
 */
@Data
@Builder
public class AliasAssignment {

    /** Surrogate primary key. Null until persisted. */
    private Long id;

    /**
     * FK to the parent PaymentTypeAssignment.
     * Only populated for CAMT054_CREDIT payment type assignments.
     */
    private Long paymentTypeAssignmentId;

    /** The alias identifier. Max 15 characters. */
    private String aliasId;

    /** Timestamp when this alias was created. */
    private Instant createdAt;
}
