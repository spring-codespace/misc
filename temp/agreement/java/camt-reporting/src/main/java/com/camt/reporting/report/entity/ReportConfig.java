package com.camt.reporting.report.entity;

import com.camt.reporting.reference.entity.ProductPart;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ReportConfig")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ReportType", nullable = false)
    private ProductPart reportType;

    @Column(name = "ReportVersion", length = 3, nullable = false)
    private String reportVersion;

    @Column(name = "ReportFrequency", length = 35, nullable = false)
    private String reportFrequency;

    @Column(name = "Description", length = 80, nullable = false)
    private String description;

    @Column(name = "MessageRecipientId", length = 20, nullable = false)
    private String messageRecipientId;

    @Column(name = "MessageRecipientType", length = 15, nullable = false)
    private String messageRecipientType;

    @Column(name = "AccountFormat", length = 4, nullable = false)
    private String accountFormat;

    @Column(name = "IsActive", nullable = false)
    private boolean isActive;

    @Column(name = "IsPaginated", nullable = false)
    private boolean isPaginated;

    @Column(name = "IsEmptyReportAllowed", nullable = false)
    private boolean isEmptyReportAllowed;

    @Column(name = "IsBundled", nullable = false)
    private boolean isBundled;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "CreatedBy", length = 20, nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "UpdatedBy", length = 20, nullable = false)
    private String updatedBy;

    @OneToMany(mappedBy = "reportConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReportAgreementScope> reportAgreementScopes = new ArrayList<>();
}
