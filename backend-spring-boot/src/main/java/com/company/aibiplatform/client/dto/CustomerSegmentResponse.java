package com.company.aibiplatform.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerSegmentResponse {
    private String customerId;
    private String segment;
    private String churnRisk;
    private String explanation;
    private double confidence;
}