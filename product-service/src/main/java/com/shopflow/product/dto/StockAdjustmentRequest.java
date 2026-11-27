package com.shopflow.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Body of POST /api/v1/products/{id}/stock-adjustments.
 * Whether stock may go negative is a DOMAIN rule (409), not checked here.
 */
@Schema(description = "A stock movement: positive adds, negative removes")
public record StockAdjustmentRequest(

        @Schema(description = "Change in units", example = "-2")
        @NotNull
        @Min(-10_000)
        @Max(10_000)
        Integer delta,

        @Schema(description = "Why the stock changed (logged)", example = "Damaged in transit")
        @NotBlank
        @Size(max = 200)
        String reason
) {
}