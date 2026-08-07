package com.company.aibiplatform.controller;

import com.company.aibiplatform.dto.dashboard.DashboardInsightResponse;
import com.company.aibiplatform.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /** GET /api/dashboard/insights — exactly the endpoint described in the README's sequence diagram. */
    @GetMapping("/insights")
    public ResponseEntity<DashboardInsightResponse> getInsights() {
        return ResponseEntity.ok(dashboardService.getInsights());
    }
}