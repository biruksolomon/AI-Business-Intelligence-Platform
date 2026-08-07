package com.company.aibiplatform.service;

import com.company.aibiplatform.dto.sale.SaleItemRequest;
import com.company.aibiplatform.dto.sale.SaleItemResponse;
import com.company.aibiplatform.dto.sale.SaleRequest;
import com.company.aibiplatform.dto.sale.SaleResponse;
import com.company.aibiplatform.entity.Customer;
import com.company.aibiplatform.entity.Product;
import com.company.aibiplatform.entity.Sale;
import com.company.aibiplatform.entity.SaleItem;
import com.company.aibiplatform.entity.User;
import com.company.aibiplatform.exception.ResourceNotFoundException;
import com.company.aibiplatform.repository.CustomerRepository;
import com.company.aibiplatform.repository.ProductRepository;
import com.company.aibiplatform.repository.SaleRepository;
import com.company.aibiplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    /**
     * Creates a sale, snapshots unit prices, and deducts stock — all inside
     * one transaction so a failure partway through (e.g. insufficient
     * stock on item 3 of 5) rolls back the entire sale instead of leaving
     * inventory half-adjusted.
     */
    @Transactional
    public SaleResponse createSale(SaleRequest request, Authentication authentication) {
        User employee = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + request.getCustomerId()));
        }

        Sale sale = Sale.builder()
                .customer(customer)
                .employee(employee)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (SaleItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemRequest.getProductId()));

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for '" + product.getName() + "' — have "
                                + product.getStockQuantity() + ", requested " + itemRequest.getQuantity()
                );
            }

            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepository.save(product);

            SaleItem saleItem = SaleItem.builder()
                    .sale(sale)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            sale.getItems().add(saleItem);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }

        sale.setTotalAmount(total);
        Sale saved = saleRepository.save(sale);

        return toResponse(saved);
    }

    public SaleResponse getById(Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found: " + id));
        return toResponse(sale);
    }

    public List<SaleResponse> getAll() {
        return saleRepository.findAll().stream().map(this::toResponse).toList();
    }

    private SaleResponse toResponse(Sale sale) {
        List<SaleItemResponse> items = sale.getItems().stream()
                .map(item -> SaleItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .lineTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();

        return SaleResponse.builder()
                .id(sale.getId())
                .customerId(sale.getCustomer() != null ? sale.getCustomer().getId() : null)
                .customerName(sale.getCustomer() != null ? sale.getCustomer().getName() : "Walk-in")
                .employeeName(sale.getEmployee().getFullName())
                .totalAmount(sale.getTotalAmount())
                .saleDate(sale.getSaleDate())
                .items(items)
                .build();
    }
}