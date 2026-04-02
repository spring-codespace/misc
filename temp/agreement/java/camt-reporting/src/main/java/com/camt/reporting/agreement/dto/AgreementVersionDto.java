package com.camt.reporting.agreement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AgreementVersionDto {

    @Data
    public static class CreateRequest {

        @NotBlank
        @Size(max = 20)
        private String pricingOrderRef;
    }

    @Data
    public static class Response {
        private Long id;
        private String agreementId;
        private String status;
        private String pricingOrderRef;
        private String createdAt;
        private String activatedAt;
        private String supersededAt;
        private String cancelledAt;
        private String expiredAt;
    }
}
