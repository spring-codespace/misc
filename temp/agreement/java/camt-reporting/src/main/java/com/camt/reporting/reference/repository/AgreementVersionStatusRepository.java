package com.camt.reporting.reference.repository;

import com.camt.reporting.reference.entity.AgreementVersionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgreementVersionStatusRepository extends JpaRepository<AgreementVersionStatus, String> {
}
