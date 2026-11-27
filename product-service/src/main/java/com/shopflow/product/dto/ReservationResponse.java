package com.shopflow.product.dto;

import java.util.List;

/** Status as text: controllers expose DTOs only (ApiLayerBoundaryTest), never domain enums. */
public record ReservationResponse(Long orderId, String status, List<Line> lines) {

    public record Line(Long productId, int quantity) {
    }
}