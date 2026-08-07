package com.company.aibiplatform.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String sku;

    @NotNull
    private Long categoryId;

    @NotNull
    @PositiveOrZero
    private BigDecimal price;

    @NotNull
    @PositiveOrZero
    private BigDecimal costPrice;

    @NotNull
    @PositiveOrZero
    private Integer stockQuantity;

    @NotNull
    @PositiveOrZero
    private Integer lowStockThreshold;
}