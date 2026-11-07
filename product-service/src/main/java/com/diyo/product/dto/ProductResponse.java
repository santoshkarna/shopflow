package com.diyo.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ProductResponse", description = "Response for a product")
public record ProductResponse(
    @Schema(example = "1", description = "ID of the product")
    Long id,
    @Schema(example = "Laptop", description = "Name of the product")
    String name,
    @Schema(example = "High-performance laptop for gaming and productivity", description = "Description of the product")
    String description,
    @Schema(example = "1299.99", description = "Price of the product")
    Double price
) {
}
