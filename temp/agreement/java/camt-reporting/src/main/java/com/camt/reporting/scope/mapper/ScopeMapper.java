package com.camt.reporting.scope.mapper;

import com.camt.reporting.scope.dto.ScopeDto;
import com.camt.reporting.scope.entity.AccountAssignment;
import com.camt.reporting.scope.entity.AgreementScope;
import com.camt.reporting.scope.entity.AliasAssignment;
import com.camt.reporting.scope.entity.PaymentTypeAssignment;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ScopeMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public ScopeDto.ScopeResponse toScopeResponse(AgreementScope scope) {
        ScopeDto.ScopeResponse response = new ScopeDto.ScopeResponse();
        response.setId(scope.getId());
        response.setAgreementVersionId(scope.getAgreementVersion().getId());
        response.setMessageRecipientId(scope.getMessageRecipientId());
        response.setProductPart(scope.getProductPart().getCode());
        response.setStatus(scope.getStatus().getCode());
        response.setCreatedAt(scope.getCreatedAt() != null
                ? scope.getCreatedAt().format(FORMATTER) : null);
        response.setActivatedAt(scope.getActivatedAt() != null
                ? scope.getActivatedAt().format(FORMATTER) : null);
        response.setCancelledAt(scope.getCancelledAt() != null
                ? scope.getCancelledAt().format(FORMATTER) : null);
        response.setPaymentTypeAssignments(
                scope.getPaymentTypeAssignments().stream()
                        .map(this::toPaymentTypeAssignmentResponse)
                        .toList());
        return response;
    }

    public ScopeDto.PaymentTypeAssignmentResponse toPaymentTypeAssignmentResponse(PaymentTypeAssignment pta) {
        ScopeDto.PaymentTypeAssignmentResponse response = new ScopeDto.PaymentTypeAssignmentResponse();
        response.setId(pta.getId());
        response.setPaymentTypeCode(pta.getPaymentType().getCode());
        response.setCreatedAt(pta.getCreatedAt() != null
                ? pta.getCreatedAt().format(FORMATTER) : null);
        response.setAccounts(pta.getAccountAssignments().stream()
                .map(this::toAccountAssignmentResponse)
                .toList());
        response.setAliases(pta.getAliasAssignments().stream()
                .map(this::toAliasAssignmentResponse)
                .toList());
        return response;
    }

    public ScopeDto.AccountAssignmentResponse toAccountAssignmentResponse(AccountAssignment account) {
        ScopeDto.AccountAssignmentResponse response = new ScopeDto.AccountAssignmentResponse();
        response.setId(account.getId());
        response.setAccountBban(account.getAccountBban());
        response.setAccountIban(account.getAccountIban());
        response.setCurrency(account.getCurrency());
        response.setCreatedAt(account.getCreatedAt() != null
                ? account.getCreatedAt().format(FORMATTER) : null);
        return response;
    }

    public ScopeDto.AliasAssignmentResponse toAliasAssignmentResponse(AliasAssignment alias) {
        ScopeDto.AliasAssignmentResponse response = new ScopeDto.AliasAssignmentResponse();
        response.setId(alias.getId());
        response.setAliasId(alias.getAliasId());
        response.setCreatedAt(alias.getCreatedAt() != null
                ? alias.getCreatedAt().format(FORMATTER) : null);
        return response;
    }

    public List<ScopeDto.ScopeResponse> toScopeResponseList(List<AgreementScope> scopes) {
        return scopes.stream().map(this::toScopeResponse).toList();
    }
}
