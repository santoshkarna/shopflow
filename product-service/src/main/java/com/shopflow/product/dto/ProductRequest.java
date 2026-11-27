package com.shopflow.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "A complete product as sent by clients (no id: the server assigns it)")
public record ProductRequest(

        @Schema(description = "Product name", example = "Philips Air Fryer")
        @NotBlank
        @Size(max = 200)
        String name,

        @Schema(description = "Category name; created if it does not exist (case-insensitive match)", example = "Kitchen")
        @NotBlank
        @Size(max = 100)
        String category,

        @Schema(description = "Price with exactly two decimals", example = "14500.00")
        @NotNull
        @DecimalMin("0.01")
        @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        @Schema(description = "Units in stock", example = "20")
        @NotNull
        @PositiveOrZero
        Integer stockQuantity
) {
}
