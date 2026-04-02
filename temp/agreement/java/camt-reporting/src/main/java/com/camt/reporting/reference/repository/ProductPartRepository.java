package com.camt.reporting.reference.repository;

import com.camt.reporting.reference.entity.ProductPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductPartRepository extends JpaRepository<ProductPart, String> {
}
