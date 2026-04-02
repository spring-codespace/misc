package com.camt.reporting.reference.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "AgreementScopeStatus")
@Getter
@Setter
@NoArgsConstructor
public class AgreementScopeStatus {

    @Id
    @Column(name = "Code", length = 15, nullable = false)
    private String code;

    @Column(name = "Description", length = 100, nullable = false)
    private String description;
}
