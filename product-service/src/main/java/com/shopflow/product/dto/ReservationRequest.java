package com.shopflow.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/** All lines of ONE order. Lines for the same product are summed. */
public record ReservationRequest(
        @NotEmpty @Size(max = 20) List<@Valid @NotNull Line> lines) {

    public record Line(
            @Schema(example = "101") @NotNull @Positive Long productId,
            @Schema(example = "2") @Positive int quantity) {
    }
}