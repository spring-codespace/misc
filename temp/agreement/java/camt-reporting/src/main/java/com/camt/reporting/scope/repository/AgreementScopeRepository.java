package com.camt.reporting.scope.repository;

import com.camt.reporting.scope.entity.AgreementScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgreementScopeRepository extends JpaRepository<AgreementScope, Long> {

    List<AgreementScope> findByAgreementVersionId(Long agreementVersionId);

    List<AgreementScope> findByMessageRecipientId(String messageRecipientId);

    Optional<AgreementScope> findByAgreementVersionIdAndMessageRecipientIdAndProductPartCode(
            Long agreementVersionId, String messageRecipientId, String productPartCode);

    boolean existsByAgreementVersionIdAndMessageRecipientIdAndProductPartCode(
            Long agreementVersionId, String messageRecipientId, String productPartCode);
}
