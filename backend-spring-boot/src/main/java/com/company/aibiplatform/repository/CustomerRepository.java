package com.company.aibiplatform.repository;

import com.company.aibiplatform.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}