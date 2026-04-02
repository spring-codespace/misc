package com.camt.reporting.cart.repository;

import com.camt.reporting.agreement.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByAgreementVersionId(Long agreementVersionId);

    List<CartItem> findByCorporateId(String corporateId);

    @Query("SELECT c FROM CartItem c WHERE c.expiresAt <= :now AND c.approvedAt IS NULL AND c.expiredAt IS NULL")
    List<CartItem> findPendingExpired(@Param("now") LocalDateTime now);
}
