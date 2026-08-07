package com.company.aibiplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestockRecommendationRequest {
    private List<String> products;
    private List<Integer> stockLevels;
    private List<List<Double>> historicalSales;
}