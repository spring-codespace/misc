package com.camt.reporting.scope.entity;

import com.camt.reporting.agreement.entity.AgreementVersion;
import com.camt.reporting.reference.entity.AgreementScopeStatus;
import com.camt.reporting.reference.entity.ProductPart;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "AgreementScope")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgreementScope {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AgreementVersionId", nullable = false)
    private AgreementVersion agreementVersion;

    @Column(name = "MessageRecipientId", length = 20, nullable = false)
    private String messageRecipientId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ProductPart", nullable = false)
    private ProductPart productPart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Status", nullable = false)
    private AgreementScopeStatus status;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "ActivatedAt")
    private LocalDateTime activatedAt;

    @Column(name = "CancelledAt")
    private LocalDateTime cancelledAt;

    @OneToMany(mappedBy = "agreementScope", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PaymentTypeAssignment> paymentTypeAssignments = new ArrayList<>();
}
