package com.company.aibiplatform.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerSegmentRequest {
    private String customerId;
    private double totalSpent;
    private int purchaseFrequency;
    private int recencyDays;
    private double averageOrderValue;
}