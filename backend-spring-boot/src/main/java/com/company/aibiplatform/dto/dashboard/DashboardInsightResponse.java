package com.company.aibiplatform.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Merges business data (Spring Boot / DB) with AI predictions (FastAPI
 * service) into the single response the dashboard actually renders.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardInsightResponse {
    private BigDecimal totalRevenueLast30Days;
    private long totalSalesLast30Days;
    private int lowStockProductCount;

    private String salesTrend;              // "rising" | "declining" | "stable"
    private double salesPercentageChange;
    private String salesExplanation;

    private List<String> lowStockProductNames;
    private List<String> restockRecommendations;
}