package com.company.aibiplatform.client;

import com.company.aibiplatform.client.dto.CustomerSegmentRequest;
import com.company.aibiplatform.client.dto.CustomerSegmentResponse;
import com.company.aibiplatform.client.dto.RestockRecommendationRequest;
import com.company.aibiplatform.client.dto.RestockRecommendationResponse;
import com.company.aibiplatform.client.dto.SalesForecastRequest;
import com.company.aibiplatform.client.dto.SalesForecastResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Declarative HTTP client for the Python AI microservice. Spring generates
 * the implementation at startup — we only declare the method signatures and
 * the URL comes from `ai.service.url` in application.yml. This is exactly
 * the "Spring Boot -> POST /predict/..." call described in the README.
 */
@FeignClient(name = "ai-service", url = "${ai.service.url}")
public interface AiServiceClient {

    @PostMapping("/predict/sales-forecast")
    SalesForecastResponse forecastSales(@RequestBody SalesForecastRequest request);

    @PostMapping("/predict/customer-segment")
    CustomerSegmentResponse segmentCustomer(@RequestBody CustomerSegmentRequest request);

    @PostMapping("/predict/restock-recommendation")
    RestockRecommendationResponse recommendRestock(@RequestBody RestockRecommendationRequest request);
}