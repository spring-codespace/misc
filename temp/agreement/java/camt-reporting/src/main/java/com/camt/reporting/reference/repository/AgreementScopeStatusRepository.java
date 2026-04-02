package com.camt.reporting.reference.repository;

import com.camt.reporting.reference.entity.AgreementScopeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgreementScopeStatusRepository extends JpaRepository<AgreementScopeStatus, String> {
}
