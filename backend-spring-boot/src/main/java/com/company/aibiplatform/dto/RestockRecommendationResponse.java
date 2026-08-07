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
public class RestockRecommendationResponse {
    private List<RestockItem> restockPriorities;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RestockItem {
        private String productId;
        private int priority;
        private String reason;
    }
}