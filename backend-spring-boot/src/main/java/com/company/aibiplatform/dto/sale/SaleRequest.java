package com.company.aibiplatform.dto.sale;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SaleRequest {

    /** Nullable — walk-in sales with no customer profile are allowed. */
    private Long customerId;

    @NotEmpty
    @Valid
    private List<SaleItemRequest> items;
}