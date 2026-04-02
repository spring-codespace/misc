package com.camt.reporting.report.mapper;

import com.camt.reporting.report.dto.ReportDto;
import com.camt.reporting.report.entity.ReportAgreementScope;
import com.camt.reporting.report.entity.ReportConfig;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ReportMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public ReportDto.Response toResponse(ReportConfig config) {
        ReportDto.Response response = new ReportDto.Response();
        response.setId(config.getId());
        response.setReportTypeCode(config.getReportType().getCode());
        response.setReportVersion(config.getReportVersion());
        response.setReportFrequency(config.getReportFrequency());
        response.setDescription(config.getDescription());
        response.setMessageRecipientId(config.getMessageRecipientId());
        response.setMessageRecipientType(config.getMessageRecipientType());
        response.setAccountFormat(config.getAccountFormat());
        response.setActive(config.isActive());
        response.setPaginated(config.isPaginated());
        response.setEmptyReportAllowed(config.isEmptyReportAllowed());
        response.setBundled(config.isBundled());
        response.setCreatedAt(config.getCreatedAt() != null
                ? config.getCreatedAt().format(FORMATTER) : null);
        response.setCreatedBy(config.getCreatedBy());
        response.setUpdatedAt(config.getUpdatedAt() != null
                ? config.getUpdatedAt().format(FORMATTER) : null);
        response.setUpdatedBy(config.getUpdatedBy());
        response.setAgreementScopes(config.getReportAgreementScopes().stream()
                .map(this::toScopeLinkResponse)
                .toList());
        return response;
    }

    public ReportDto.ReportAgreementScopeResponse toScopeLinkResponse(ReportAgreementScope link) {
        ReportDto.ReportAgreementScopeResponse response = new ReportDto.ReportAgreementScopeResponse();
        response.setId(link.getId());
        response.setAgreementScopeId(link.getAgreementScope().getId());
        response.setAgreementId(link.getAgreement().getId());
        return response;
    }

    public List<ReportDto.Response> toResponseList(List<ReportConfig> configs) {
        return configs.stream().map(this::toResponse).toList();
    }
}
