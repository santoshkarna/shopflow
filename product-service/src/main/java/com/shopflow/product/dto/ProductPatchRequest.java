package com.shopflow.product.dto;

import com.shopflow.product.validation.NullOrNotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Body of PATCH /api/v1/products/{id}: merge semantics.
 * Every field is OPTIONAL (null = leave unchanged), but any field that is present must be valid.
 */
@Schema(description = """
        Partial update with merge semantics: send ONLY the fields to change.
        Absent or null fields are left unchanged; no field can be cleared.""",
        example = "{\"price\": 79999.00}")
public record ProductPatchRequest(

        @Schema(description = "New name, if changing", example = "Samsung Air Conditioner 1.5T")
        @NullOrNotBlank
        @Size(max = 200)
        String name,

        @Schema(description = "New category, if changing (created if new)", example = "Home Appliances")
        @NullOrNotBlank
        @Size(max = 100)
        String category,

        @Schema(description = "New price, if changing", example = "79999.00")
        @DecimalMin("0.01")
        @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        @Schema(description = "New stock level, if setting an absolute value (use stock-adjustments for +/- changes)",
                example = "12")
        @PositiveOrZero
        Integer stockQuantity
) {
}
