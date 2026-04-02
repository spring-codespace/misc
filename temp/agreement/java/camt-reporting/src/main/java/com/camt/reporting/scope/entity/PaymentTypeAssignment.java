package com.camt.reporting.scope.entity;

import com.camt.reporting.reference.entity.PaymentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PaymentTypeAssignment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTypeAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AgreementScopeId", nullable = false)
    private AgreementScope agreementScope;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PaymentType", nullable = false)
    private PaymentType paymentType;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "paymentTypeAssignment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AccountAssignment> accountAssignments = new ArrayList<>();

    @OneToMany(mappedBy = "paymentTypeAssignment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AliasAssignment> aliasAssignments = new ArrayList<>();
}
