package com.camt.reporting.agreement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "AgreementContact")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgreementContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AgreementId", nullable = false)
    private Agreement agreement;

    @Column(name = "ContactName", length = 40, nullable = false)
    private String contactName;

    @Column(name = "ContactEmail", length = 50, nullable = false)
    private String contactEmail;

    @Column(name = "ContactPhone", length = 15, nullable = false)
    private String contactPhone;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
