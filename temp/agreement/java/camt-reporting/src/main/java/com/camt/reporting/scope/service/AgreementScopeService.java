package com.camt.reporting.scope.service;

import com.camt.reporting.agreement.entity.AgreementVersion;
import com.camt.reporting.agreement.service.AgreementService;
import com.camt.reporting.common.exception.BusinessException;
import com.camt.reporting.common.exception.ResourceNotFoundException;
import com.camt.reporting.reference.entity.AgreementScopeStatus;
import com.camt.reporting.reference.entity.PaymentType;
import com.camt.reporting.reference.entity.ProductPart;
import com.camt.reporting.reference.repository.AgreementScopeStatusRepository;
import com.camt.reporting.reference.repository.PaymentTypeRepository;
import com.camt.reporting.reference.repository.ProductPartRepository;
import com.camt.reporting.scope.dto.ScopeDto;
import com.camt.reporting.scope.entity.AccountAssignment;
import com.camt.reporting.scope.entity.AgreementScope;
import com.camt.reporting.scope.entity.AliasAssignment;
import com.camt.reporting.scope.entity.PaymentTypeAssignment;
import com.camt.reporting.scope.mapper.ScopeMapper;
import com.camt.reporting.scope.repository.AgreementScopeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgreementScopeService {

    private static final String STATUS_DRAFT    = "DRAFT";
    private static final String STATUS_ACTIVE   = "ACTIVE";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private final AgreementScopeRepository scopeRepository;
    private final AgreementScopeStatusRepository scopeStatusRepository;
    private final ProductPartRepository productPartRepository;
    private final PaymentTypeRepository paymentTypeRepository;
    private final AgreementService agreementService;
    private final ScopeMapper mapper;

    @Transactional
    public ScopeDto.ScopeResponse addScope(Long versionId, ScopeDto.CreateScopeRequest request) {
        AgreementVersion version = agreementService.findVersionEntity(versionId);

        if (!"DRAFT".equals(version.getStatus().getCode())) {
            throw new BusinessException("Scopes can only be added to a DRAFT version.");
        }

        if (scopeRepository.existsByAgreementVersionIdAndMessageRecipientIdAndProductPartCode(
                versionId, request.getMessageRecipientId(), request.getProductPartCode())) {
            throw new BusinessException("Scope already exists for this version, recipient and product part.");
        }

        ProductPart productPart = productPartRepository.findById(request.getProductPartCode())
                .orElseThrow(() -> new BusinessException("Unknown ProductPart: " + request.getProductPartCode()));

        AgreementScope scope = AgreementScope.builder()
                .agreementVersion(version)
                .messageRecipientId(request.getMessageRecipientId())
                .productPart(productPart)
                .status(resolveScopeStatus(STATUS_DRAFT))
                .createdAt(LocalDateTime.now())
                .build();

        buildPaymentTypeAssignments(request.getPaymentTypeAssignments(), scope);

        return mapper.toScopeResponse(scopeRepository.save(scope));
    }

    @Transactional
    public ScopeDto.ScopeResponse cancelScope(Long scopeId) {
        AgreementScope scope = findScope(scopeId);

        if (STATUS_CANCELLED.equals(scope.getStatus().getCode())) {
            throw new BusinessException("Scope is already cancelled.");
        }

        scope.setStatus(resolveScopeStatus(STATUS_CANCELLED));
        scope.setCancelledAt(LocalDateTime.now());

        return mapper.toScopeResponse(scopeRepository.save(scope));
    }

    @Transactional(readOnly = true)
    public List<ScopeDto.ScopeResponse> getScopesByVersion(Long versionId) {
        agreementService.findVersionEntity(versionId);
        return mapper.toScopeResponseList(scopeRepository.findByAgreementVersionId(versionId));
    }

    @Transactional(readOnly = true)
    public ScopeDto.ScopeResponse getScope(Long scopeId) {
        return mapper.toScopeResponse(findScope(scopeId));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void buildPaymentTypeAssignments(List<ScopeDto.PaymentTypeAssignmentRequest> requests,
                                             AgreementScope scope) {
        requests.forEach(ptaRequest -> {
            PaymentType paymentType = paymentTypeRepository.findById(ptaRequest.getPaymentTypeCode())
                    .orElseThrow(() -> new BusinessException(
                            "Unknown PaymentType: " + ptaRequest.getPaymentTypeCode()));

            PaymentTypeAssignment pta = PaymentTypeAssignment.builder()
                    .agreementScope(scope)
                    .paymentType(paymentType)
                    .createdAt(LocalDateTime.now())
                    .build();

            ptaRequest.getAccounts().forEach(accRequest -> {
                AccountAssignment account = AccountAssignment.builder()
                        .paymentTypeAssignment(pta)
                        .accountBban(accRequest.getAccountBban())
                        .accountIban(accRequest.getAccountIban())
                        .currency(accRequest.getCurrency())
                        .createdAt(LocalDateTime.now())
                        .build();
                pta.getAccountAssignments().add(account);
            });

            if (ptaRequest.getAliasIds() != null) {
                ptaRequest.getAliasIds().forEach(aliasId -> {
                    AliasAssignment alias = AliasAssignment.builder()
                            .paymentTypeAssignment(pta)
                            .aliasId(aliasId)
                            .createdAt(LocalDateTime.now())
                            .build();
                    pta.getAliasAssignments().add(alias);
                });
            }

            scope.getPaymentTypeAssignments().add(pta);
        });
    }

    public AgreementScope findScopeEntity(Long scopeId) {
        return findScope(scopeId);
    }

    private AgreementScope findScope(Long scopeId) {
        return scopeRepository.findById(scopeId)
                .orElseThrow(() -> ResourceNotFoundException.of("AgreementScope", scopeId));
    }

    private AgreementScopeStatus resolveScopeStatus(String code) {
        return scopeStatusRepository.findById(code)
                .orElseThrow(() -> new BusinessException("Unknown AgreementScopeStatus: " + code));
    }
}
