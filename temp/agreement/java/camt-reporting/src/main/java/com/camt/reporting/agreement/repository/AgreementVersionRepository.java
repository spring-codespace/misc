package com.camt.reporting.agreement.repository;

import com.camt.reporting.agreement.entity.AgreementVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgreementVersionRepository extends JpaRepository<AgreementVersion, Long> {

    List<AgreementVersion> findByAgreementId(String agreementId);

    List<AgreementVersion> findByAgreementIdAndStatusCode(String agreementId, String statusCode);

    @Query("SELECT v FROM AgreementVersion v WHERE v.agreement.id = :agreementId AND v.status.code = 'ACTIVE'")
    Optional<AgreementVersion> findActiveByAgreementId(@Param("agreementId") String agreementId);

    boolean existsByAgreementIdAndStatusCode(String agreementId, String statusCode);
}
