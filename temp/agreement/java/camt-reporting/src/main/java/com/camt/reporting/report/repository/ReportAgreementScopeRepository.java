package com.camt.reporting.report.repository;

import com.camt.reporting.report.entity.ReportAgreementScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportAgreementScopeRepository extends JpaRepository<ReportAgreementScope, Long> {

    List<ReportAgreementScope> findByReportConfigId(Long reportConfigId);

    List<ReportAgreementScope> findByAgreementId(String agreementId);

    List<ReportAgreementScope> findByAgreementScopeId(Long agreementScopeId);

    boolean existsByReportConfigIdAndAgreementScopeId(Long reportConfigId, Long agreementScopeId);
}
