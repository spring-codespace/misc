package com.camt.reporting.cart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class CartItemDto {

    @Data
    public static class CreateRequest {

        @NotNull
        private Long agreementVersionId;

        @NotBlank
        @Size(max = 15)
        private String corporateId;
    }

    @Data
    public static class Response {
        private Long id;
        private Long agreementVersionId;
        private String corporateId;
        private String expiresAt;
        private String approvedAt;
        private String expiredAt;
        private String createdAt;
        private String status;
    }
}
