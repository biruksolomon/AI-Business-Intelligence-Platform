package com.company.aibiplatform.service;

import com.company.aibiplatform.client.AiServiceClient;
import com.company.aibiplatform.client.dto.SalesForecastRequest;
import com.company.aibiplatform.client.dto.SalesForecastResponse;
import com.company.aibiplatform.dto.dashboard.DashboardInsightResponse;
import com.company.aibiplatform.entity.Product;
import com.company.aibiplatform.entity.Sale;
import com.company.aibiplatform.repository.ProductRepository;
import com.company.aibiplatform.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the class the README describes as the "brain" that merges
 * business data pulled from PostgreSQL with a prediction pulled from the
 * FastAPI AI service, producing the single response the dashboard renders.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final AiServiceClient aiServiceClient;

    public DashboardInsightResponse getInsights() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);

        List<Sale> recentSales = saleRepository.findBetween(thirtyDaysAgo, now);

        BigDecimal totalRevenue = recentSales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Product> lowStockProducts = productRepository.findLowStockProducts();

        // Build a simple daily revenue series over the last 30 days to feed
        // the AI forecasting endpoint. In a real system this would be a
        // proper SQL aggregation; grouping in-memory here keeps the demo
        // self-contained.
        List<Double> dailySales = buildDailySeries(recentSales, thirtyDaysAgo, now);

        SalesForecastResponse forecast;
        try {
            forecast = aiServiceClient.forecastSales(
                    SalesForecastRequest.builder()
                            .productCategory("all")
                            .historicalSales(dailySales)
                            .periodDays(30)
                            .build()
            );
        } catch (Exception ex) {
            // If the AI service is down, the dashboard should still work —
            // just without the predictive part. Never let an AI outage take
            // down core business reporting.
            forecast = SalesForecastResponse.builder()
                    .trend("stable")
                    .percentageChange(0)
                    .explanation("AI service unavailable — showing business data only.")
                    .confidence(0)
                    .build();
        }

        List<String> lowStockNames = lowStockProducts.stream().map(Product::getName).toList();
        List<String> restockRecommendations = lowStockProducts.stream()
                .map(p -> "Reorder '" + p.getName() + "' — " + p.getStockQuantity() + " left (threshold "
                        + p.getLowStockThreshold() + ")")
                .toList();

        return DashboardInsightResponse.builder()
                .totalRevenueLast30Days(totalRevenue)
                .totalSalesLast30Days(recentSales.size())
                .lowStockProductCount(lowStockProducts.size())
                .salesTrend(forecast.getTrend())
                .salesPercentageChange(forecast.getPercentageChange())
                .salesExplanation(forecast.getExplanation())
                .lowStockProductNames(lowStockNames)
                .restockRecommendations(restockRecommendations)
                .build();
    }

    private List<Double> buildDailySeries(List<Sale> sales, LocalDateTime start, LocalDateTime end) {
        int days = 30;
        double[] buckets = new double[days];

        for (Sale sale : sales) {
            long dayIndex = java.time.temporal.ChronoUnit.DAYS.between(start, sale.getSaleDate());
            int idx = (int) Math.min(Math.max(dayIndex, 0), days - 1);
            buckets[idx] += sale.getTotalAmount().doubleValue();
        }

        List<Double> series = new ArrayList<>();
        for (double v : buckets) {
            series.add(v);
        }
        return series;
    }
}