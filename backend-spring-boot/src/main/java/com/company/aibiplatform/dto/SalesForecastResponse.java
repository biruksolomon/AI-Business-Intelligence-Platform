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
public class SalesForecastResponse {
    private List<Double> forecast;
    private String trend;
    private double percentageChange;
    private String explanation;
    private double confidence;
}