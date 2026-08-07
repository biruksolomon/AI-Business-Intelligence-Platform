package com.company.aibiplatform.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Mirrors ai-service-python's SalesForecastRequest schema field-for-field. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesForecastRequest {
    private String productCategory;
    private List<Double> historicalSales;
    private int periodDays;
}