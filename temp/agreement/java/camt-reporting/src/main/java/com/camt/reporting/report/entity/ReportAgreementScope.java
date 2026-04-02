package com.camt.reporting.report.entity;

import com.camt.reporting.agreement.entity.Agreement;
import com.camt.reporting.scope.entity.AgreementScope;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ReportAgreementScope")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportAgreementScope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ReportConfigId", nullable = false)
    private ReportConfig reportConfig;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AgreementScopeId", nullable = false)
    private AgreementScope agreementScope;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AgreementId", nullable = false)
    private Agreement agreement;
}
