package com.shopflow.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "A product as returned by the API")
public record ProductResponse(

        @Schema(description = "Server-assigned id", example = "101")
        Long id,

        @Schema(example = "Samsung Air Conditioner")
        String name,

        @Schema(example = "Home Appliances")
        String category,

        @Schema(example = "85000.00")
        BigDecimal price,

        @Schema(example = "15")
        int stockQuantity,

        @Schema(description = "Derived: stockQuantity > 0. Read-only.", example = "true")
        boolean inStock
) {
}
