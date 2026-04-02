package com.camt.reporting.report.repository;

import com.camt.reporting.report.entity.ReportConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportConfigRepository extends JpaRepository<ReportConfig, Long> {

    List<ReportConfig> findByMessageRecipientId(String messageRecipientId);

    List<ReportConfig> findByReportTypeCode(String reportTypeCode);

    List<ReportConfig> findByIsActiveTrue();
}
