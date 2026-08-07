package com.company.aibiplatform.service;

import com.company.aibiplatform.dto.customer.CustomerRequest;
import com.company.aibiplatform.dto.customer.CustomerResponse;
import com.company.aibiplatform.entity.Customer;
import com.company.aibiplatform.exception.ResourceNotFoundException;
import com.company.aibiplatform.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerResponse create(CustomerRequest request) {
        Customer customer = Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();
        return toResponse(customerRepository.save(customer));
    }

    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = getOrThrow(id);
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        return toResponse(customerRepository.save(customer));
    }

    public void delete(Long id) {
        customerRepository.delete(getOrThrow(id));
    }

    public CustomerResponse getById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public List<CustomerResponse> getAll() {
        return customerRepository.findAll().stream().map(this::toResponse).toList();
    }

    public Customer getEntityOrThrow(Long id) {
        return getOrThrow(id);
    }

    private Customer getOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    private CustomerResponse toResponse(Customer c) {
        return CustomerResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .joinedAt(c.getJoinedAt())
                .build();
    }
}