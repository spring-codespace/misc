package com.camt.reporting.agreement.repository;

import com.camt.reporting.agreement.entity.AgreementContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgreementContactRepository extends JpaRepository<AgreementContact, Long> {

    List<AgreementContact> findByAgreementId(String agreementId);

    void deleteByAgreementId(String agreementId);
}
