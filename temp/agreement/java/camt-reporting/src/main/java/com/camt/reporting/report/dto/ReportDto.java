package com.camt.reporting.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

public class ReportDto {

    // -------------------------------------------------------------------------
    // ReportConfig
    // -------------------------------------------------------------------------

    @Data
    public static class CreateRequest {

        @NotBlank
        @Size(max = 40)
        private String reportTypeCode;

        @NotBlank
        @Size(max = 3)
        private String reportVersion;

        @NotBlank
        @Size(max = 35)
        private String reportFrequency;

        @NotBlank
        @Size(max = 80)
        private String description;

        @NotBlank
        @Size(max = 20)
        private String messageRecipientId;

        @NotBlank
        @Size(max = 15)
        private String messageRecipientType;

        @NotBlank
        @Size(max = 4)
        private String accountFormat;

        private boolean isActive;
        private boolean isPaginated;
        private boolean isEmptyReportAllowed;
        private boolean isBundled;

        @NotBlank
        @Size(max = 20)
        private String createdBy;
    }

    @Data
    public static class UpdateRequest {

        @NotBlank
        @Size(max = 35)
        private String reportFrequency;

        @NotBlank
        @Size(max = 80)
        private String description;

        @NotBlank
        @Size(max = 4)
        private String accountFormat;

        private boolean isActive;
        private boolean isPaginated;
        private boolean isEmptyReportAllowed;
        private boolean isBundled;

        @NotBlank
        @Size(max = 20)
        private String updatedBy;
    }

    @Data
    public static class Response {
        private Long id;
        private String reportTypeCode;
        private String reportVersion;
        private String reportFrequency;
        private String description;
        private String messageRecipientId;
        private String messageRecipientType;
        private String accountFormat;
        private boolean isActive;
        private boolean isPaginated;
        private boolean isEmptyReportAllowed;
        private boolean isBundled;
        private String createdAt;
        private String createdBy;
        private String updatedAt;
        private String updatedBy;
        private List<ReportAgreementScopeResponse> agreementScopes;
    }

    // -------------------------------------------------------------------------
    // ReportAgreementScope
    // -------------------------------------------------------------------------

    @Data
    public static class LinkScopeRequest {

        @NotNull
        private Long agreementScopeId;

        @NotBlank
        @Size(max = 20)
        private String agreementId;
    }

    @Data
    public static class ReportAgreementScopeResponse {
        private Long id;
        private Long agreementScopeId;
        private String agreementId;
    }
}
