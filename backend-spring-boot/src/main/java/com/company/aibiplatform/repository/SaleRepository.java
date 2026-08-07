package com.company.aibiplatform.repository;

import com.company.aibiplatform.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    @Query("SELECT s FROM Sale s WHERE s.saleDate BETWEEN :start AND :end ORDER BY s.saleDate")
    List<Sale> findBetween(LocalDateTime start, LocalDateTime end);

    List<Sale> findByCustomerId(Long customerId);
}