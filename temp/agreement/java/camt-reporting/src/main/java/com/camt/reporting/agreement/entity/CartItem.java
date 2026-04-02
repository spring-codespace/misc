package com.camt.reporting.agreement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "CartItem")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AgreementVersionId", nullable = false, unique = true)
    private AgreementVersion agreementVersion;

    @Column(name = "CorporateId", length = 15, nullable = false)
    private String corporateId;

    @Column(name = "ExpiresAt", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "ApprovedAt")
    private LocalDateTime approvedAt;

    @Column(name = "ExpiredAt")
    private LocalDateTime expiredAt;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
