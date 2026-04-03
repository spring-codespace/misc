package com.example.camt.agreement;

import com.example.camt.agreement.value.AccountDetail;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * An account assigned to a {@link PaymentTypeAssignment}.
 *
 * <p>For CAMT054_CREDIT: one or more accounts per payment type assignment.</p>
 * <p>For all other product parts: one or more accounts per scope,
 * routed through a default PaymentTypeAssignment created at scope creation time.</p>
 *
 * <p>The same account (BBAN/IBAN) may appear across multiple scopes
 * for the same product part — no exclusivity constraint.</p>
 */
@Data
@Builder
public class AccountAssignment {

    /** Surrogate primary key. Null until persisted. */
    private Long id;

    /** FK to the parent PaymentTypeAssignment. Always populated. */
    private Long paymentTypeAssignmentId;

    /** Account identification details (BBAN, IBAN, currency). */
    private AccountDetail accountDetail;

    /** Timestamp when this assignment was created. */
    private Instant createdAt;
}
