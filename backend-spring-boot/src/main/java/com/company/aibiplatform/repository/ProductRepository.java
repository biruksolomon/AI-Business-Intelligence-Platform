package com.company.aibiplatform.repository;

import com.company.aibiplatform.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    boolean existsBySku(String sku);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.lowStockThreshold")
    List<Product> findLowStockProducts();
}