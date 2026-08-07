package com.company.aibiplatform.repository;

import com.company.aibiplatform.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
}