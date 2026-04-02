package com.camt.reporting.scope.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "AccountAssignment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PaymentTypeAssignmentId", nullable = false)
    private PaymentTypeAssignment paymentTypeAssignment;

    @Column(name = "AccountBBAN", length = 15, nullable = false)
    private String accountBban;

    @Column(name = "AccountIBAN", length = 35, nullable = false)
    private String accountIban;

    @Column(name = "Currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
