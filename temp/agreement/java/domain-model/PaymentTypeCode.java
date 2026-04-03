package com.example.camt.agreement.enums;

/**
 * Allowed payment type codes for PaymentTypeAssignment.
 * Only applicable when ProductPart = CAMT054_CREDIT.
 */
public enum PaymentTypeCode {

    CREDIT_TRANSFER,
    DIRECT_DEBIT,
    INSTANT_PAYMENT
}
