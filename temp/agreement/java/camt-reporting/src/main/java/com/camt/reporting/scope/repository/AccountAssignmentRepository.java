package com.camt.reporting.scope.repository;

import com.camt.reporting.scope.entity.AccountAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountAssignmentRepository extends JpaRepository<AccountAssignment, Long> {

    List<AccountAssignment> findByPaymentTypeAssignmentId(Long paymentTypeAssignmentId);
}
