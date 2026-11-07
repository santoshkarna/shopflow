package com.diyo.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "ProductRequest", description = "Payload to creat/update a product")
public record ProductRequest(
        @Size(min = 1, max = 50)
        @Schema(example = "Laptop", maxLength = 50, description = "Name of product")
        String name,
        @Schema(example = "High-performance laptop for gaming and productivity", description = "Description of product")
        String description,
        @NotNull
        @Schema(example = "1299.99", description = "Price of product")
        Double price
) {

}
