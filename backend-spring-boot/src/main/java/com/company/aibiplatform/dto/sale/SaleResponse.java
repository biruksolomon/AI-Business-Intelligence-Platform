package com.company.aibiplatform.dto.sale;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private String employeeName;
    private BigDecimal totalAmount;
    private LocalDateTime saleDate;
    private List<SaleItemResponse> items;
}