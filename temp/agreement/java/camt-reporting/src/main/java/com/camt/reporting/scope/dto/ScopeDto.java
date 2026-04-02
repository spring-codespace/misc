package com.camt.reporting.scope.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

public class ScopeDto {

    // -------------------------------------------------------------------------
    // AgreementScope
    // -------------------------------------------------------------------------

    @Data
    public static class CreateScopeRequest {

        @NotBlank
        @Size(max = 20)
        private String messageRecipientId;

        @NotBlank
        @Size(max = 40)
        private String productPartCode;

        @NotEmpty
        @Valid
        private List<PaymentTypeAssignmentRequest> paymentTypeAssignments;
    }

    @Data
    public static class ScopeResponse {
        private Long id;
        private Long agreementVersionId;
        private String messageRecipientId;
        private String productPart;
        private String status;
        private String createdAt;
        private String activatedAt;
        private String cancelledAt;
        private List<PaymentTypeAssignmentResponse> paymentTypeAssignments;
    }

    // -------------------------------------------------------------------------
    // PaymentTypeAssignment
    // -------------------------------------------------------------------------

    @Data
    public static class PaymentTypeAssignmentRequest {

        @NotBlank
        @Size(max = 40)
        private String paymentTypeCode;

        @NotEmpty
        @Valid
        private List<AccountAssignmentRequest> accounts;

        private List<@NotBlank @Size(max = 15) String> aliasIds;
    }

    @Data
    public static class PaymentTypeAssignmentResponse {
        private Long id;
        private String paymentTypeCode;
        private String createdAt;
        private List<AccountAssignmentResponse> accounts;
        private List<AliasAssignmentResponse> aliases;
    }

    // -------------------------------------------------------------------------
    // AccountAssignment
    // -------------------------------------------------------------------------

    @Data
    public static class AccountAssignmentRequest {

        @NotBlank
        @Size(max = 15)
        private String accountBban;

        @NotBlank
        @Size(max = 35)
        private String accountIban;

        @NotBlank
        @Size(max = 3)
        private String currency;
    }

    @Data
    public static class AccountAssignmentResponse {
        private Long id;
        private String accountBban;
        private String accountIban;
        private String currency;
        private String createdAt;
    }

    // -------------------------------------------------------------------------
    // AliasAssignment
    // -------------------------------------------------------------------------

    @Data
    public static class AliasAssignmentResponse {
        private Long id;
        private String aliasId;
        private String createdAt;
    }
}
