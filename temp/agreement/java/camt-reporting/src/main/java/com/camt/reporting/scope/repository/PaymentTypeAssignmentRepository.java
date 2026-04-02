package com.camt.reporting.scope.repository;

import com.camt.reporting.scope.entity.PaymentTypeAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTypeAssignmentRepository extends JpaRepository<PaymentTypeAssignment, Long> {

    List<PaymentTypeAssignment> findByAgreementScopeId(Long agreementScopeId);

    Optional<PaymentTypeAssignment> findByAgreementScopeIdAndPaymentTypeCode(
            Long agreementScopeId, String paymentTypeCode);

    boolean existsByAgreementScopeIdAndPaymentTypeCode(Long agreementScopeId, String paymentTypeCode);
}
