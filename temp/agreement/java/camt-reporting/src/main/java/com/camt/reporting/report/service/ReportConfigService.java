package com.camt.reporting.report.service;

import com.camt.reporting.agreement.entity.Agreement;
import com.camt.reporting.agreement.repository.AgreementRepository;
import com.camt.reporting.common.exception.BusinessException;
import com.camt.reporting.common.exception.ResourceNotFoundException;
import com.camt.reporting.reference.entity.ProductPart;
import com.camt.reporting.reference.repository.ProductPartRepository;
import com.camt.reporting.report.dto.ReportDto;
import com.camt.reporting.report.entity.ReportAgreementScope;
import com.camt.reporting.report.entity.ReportConfig;
import com.camt.reporting.report.mapper.ReportMapper;
import com.camt.reporting.report.repository.ReportAgreementScopeRepository;
import com.camt.reporting.report.repository.ReportConfigRepository;
import com.camt.reporting.scope.entity.AgreementScope;
import com.camt.reporting.scope.service.AgreementScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportConfigService {

    private final ReportConfigRepository reportConfigRepository;
    private final ReportAgreementScopeRepository reportAgreementScopeRepository;
    private final ProductPartRepository productPartRepository;
    private final AgreementRepository agreementRepository;
    private final AgreementScopeService agreementScopeService;
    private final ReportMapper mapper;

    // -------------------------------------------------------------------------
    // Create / Update
    // -------------------------------------------------------------------------

    @Transactional
    public ReportDto.Response createReportConfig(ReportDto.CreateRequest request) {
        ProductPart reportType = productPartRepository.findById(request.getReportTypeCode())
                .orElseThrow(() -> new BusinessException("Unknown ProductPart: " + request.getReportTypeCode()));

        ReportConfig config = ReportConfig.builder()
                .reportType(reportType)
                .reportVersion(request.getReportVersion())
                .reportFrequency(request.getReportFrequency())
                .description(request.getDescription())
                .messageRecipientId(request.getMessageRecipientId())
                .messageRecipientType(request.getMessageRecipientType())
                .accountFormat(request.getAccountFormat())
                .isActive(request.isActive())
                .isPaginated(request.isPaginated())
                .isEmptyReportAllowed(request.isEmptyReportAllowed())
                .isBundled(request.isBundled())
                .createdAt(LocalDateTime.now())
                .createdBy(request.getCreatedBy())
                .updatedAt(LocalDateTime.now())
                .updatedBy(request.getCreatedBy())
                .build();

        return mapper.toResponse(reportConfigRepository.save(config));
    }

    @Transactional
    public ReportDto.Response updateReportConfig(Long configId, ReportDto.UpdateRequest request) {
        ReportConfig config = findConfig(configId);

        config.setReportFrequency(request.getReportFrequency());
        config.setDescription(request.getDescription());
        config.setAccountFormat(request.getAccountFormat());
        config.setActive(request.isActive());
        config.setPaginated(request.isPaginated());
        config.setEmptyReportAllowed(request.isEmptyReportAllowed());
        config.setBundled(request.isBundled());
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy(request.getUpdatedBy());

        return mapper.toResponse(reportConfigRepository.save(config));
    }

    // -------------------------------------------------------------------------
    // Scope linking
    // -------------------------------------------------------------------------

    @Transactional
    public ReportDto.Response linkScope(Long configId, ReportDto.LinkScopeRequest request) {
        ReportConfig config = findConfig(configId);

        if (reportAgreementScopeRepository.existsByReportConfigIdAndAgreementScopeId(
                configId, request.getAgreementScopeId())) {
            throw new BusinessException("Scope " + request.getAgreementScopeId()
                    + " is already linked to report config " + configId);
        }

        AgreementScope scope = agreementScopeService.findScopeEntity(request.getAgreementScopeId());

        Agreement agreement = agreementRepository.findById(request.getAgreementId())
                .orElseThrow(() -> ResourceNotFoundException.of("Agreement", request.getAgreementId()));

        ReportAgreementScope link = ReportAgreementScope.builder()
                .reportConfig(config)
                .agreementScope(scope)
                .agreement(agreement)
                .build();

        config.getReportAgreementScopes().add(link);
        return mapper.toResponse(reportConfigRepository.save(config));
    }

    @Transactional
    public ReportDto.Response unlinkScope(Long configId, Long reportAgreementScopeId) {
        ReportConfig config = findConfig(configId);

        ReportAgreementScope link = reportAgreementScopeRepository.findById(reportAgreementScopeId)
                .orElseThrow(() -> ResourceNotFoundException.of("ReportAgreementScope", reportAgreementScopeId));

        if (!link.getReportConfig().getId().equals(configId)) {
            throw new BusinessException("ReportAgreementScope " + reportAgreementScopeId
                    + " does not belong to config " + configId);
        }

        config.getReportAgreementScopes().remove(link);
        return mapper.toResponse(reportConfigRepository.save(config));
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public ReportDto.Response getReportConfig(Long configId) {
        return mapper.toResponse(findConfig(configId));
    }

    @Transactional(readOnly = true)
    public List<ReportDto.Response> getAllActive() {
        return mapper.toResponseList(reportConfigRepository.findByIsActiveTrue());
    }

    @Transactional(readOnly = true)
    public List<ReportDto.Response> getByRecipient(String messageRecipientId) {
        return mapper.toResponseList(reportConfigRepository.findByMessageRecipientId(messageRecipientId));
    }

    @Transactional(readOnly = true)
    public List<ReportDto.Response> getByReportType(String reportTypeCode) {
        return mapper.toResponseList(reportConfigRepository.findByReportTypeCode(reportTypeCode));
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private ReportConfig findConfig(Long configId) {
        return reportConfigRepository.findById(configId)
                .orElseThrow(() -> ResourceNotFoundException.of("ReportConfig", configId));
    }
}
