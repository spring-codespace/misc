package com.camt.reporting.agreement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

public class AgreementDto {

    @Data
    public static class CreateRequest {

        @NotBlank
        @Size(max = 20)
        private String id;

        @NotBlank
        @Size(max = 35)
        private String name;

        @NotBlank
        @Size(max = 5)
        private String bankId;

        @NotBlank
        @Size(max = 15)
        private String corporateId;

        @NotBlank
        @Size(max = 20)
        private String channel;

        @NotBlank
        @Size(max = 20)
        private String pricingOrderRef;

        @NotEmpty
        @Valid
        private List<ContactRequest> contacts;
    }

    @Data
    public static class ContactRequest {

        @NotBlank
        @Size(max = 40)
        private String contactName;

        @NotBlank
        @Size(max = 50)
        private String contactEmail;

        @NotBlank
        @Size(max = 15)
        private String contactPhone;
    }

    @Data
    public static class Response {
        private String id;
        private String name;
        private String bankId;
        private String corporateId;
        private String channel;
        private String createdAt;
        private List<ContactResponse> contacts;
    }

    @Data
    public static class ContactResponse {
        private Long id;
        private String contactName;
        private String contactEmail;
        private String contactPhone;
        private String createdAt;
    }
}
