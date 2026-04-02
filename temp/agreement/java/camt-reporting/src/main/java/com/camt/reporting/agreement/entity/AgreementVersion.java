package com.camt.reporting.agreement.entity;

import com.camt.reporting.reference.entity.AgreementVersionStatus;
import com.camt.reporting.scope.entity.AgreementScope;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "AgreementVersion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgreementVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AgreementId", nullable = false)
    private Agreement agreement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Status", nullable = false)
    private AgreementVersionStatus status;

    @Column(name = "PricingOrderRef", length = 20, nullable = false)
    private String pricingOrderRef;

    @Column(name = "ExpiredAt")
    private LocalDateTime expiredAt;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "ActivatedAt")
    private LocalDateTime activatedAt;

    @Column(name = "SupersededAt")
    private LocalDateTime supersededAt;

    @Column(name = "CancelledAt")
    private LocalDateTime cancelledAt;

    @OneToMany(mappedBy = "agreementVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AgreementScope> scopes = new ArrayList<>();

    @OneToOne(mappedBy = "agreementVersion", cascade = CascadeType.ALL, orphanRemoval = true)
    private CartItem cartItem;
}
