package com.camt.reporting.agreement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Agreement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agreement {

    @Id
    @Column(name = "Id", length = 20, nullable = false)
    private String id;

    @Column(name = "Name", length = 35, nullable = false)
    private String name;

    @Column(name = "BankId", length = 5, nullable = false)
    private String bankId;

    @Column(name = "CorporateId", length = 15, nullable = false)
    private String corporateId;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Channel", length = 20, nullable = false)
    private String channel;

    @OneToMany(mappedBy = "agreement", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AgreementContact> contacts = new ArrayList<>();

    @OneToMany(mappedBy = "agreement", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AgreementVersion> versions = new ArrayList<>();
}
