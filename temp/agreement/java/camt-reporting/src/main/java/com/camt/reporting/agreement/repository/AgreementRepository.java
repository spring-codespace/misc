package com.camt.reporting.agreement.repository;

import com.camt.reporting.agreement.entity.Agreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgreementRepository extends JpaRepository<Agreement, String> {

    List<Agreement> findByCorporateId(String corporateId);

    List<Agreement> findByBankId(String bankId);

    boolean existsByIdAndCorporateId(String id, String corporateId);
}
