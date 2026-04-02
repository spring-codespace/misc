package com.camt.reporting.scope.repository;

import com.camt.reporting.scope.entity.AliasAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AliasAssignmentRepository extends JpaRepository<AliasAssignment, Long> {

    List<AliasAssignment> findByPaymentTypeAssignmentId(Long paymentTypeAssignmentId);
}
