package com.shopflow.product.controller;

import com.shopflow.product.dto.ReservationRequest;
import com.shopflow.product.dto.ReservationResponse;
import com.shopflow.product.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Contract op 8 (additive, ADR-0003): called by Order Service. */
@Tag(name = "Reservations", description = "Stock held for orders")
@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "Reserve stock for all lines of one order (all or nothing, idempotent per orderId)")
    @ApiResponse(responseCode = "201", description = "Reserved now")
    @ApiResponse(responseCode = "200", description = "Identical reservation already existed; nothing changed")
    @ApiResponse(responseCode = "404", description = "A product does not exist (product-not-found)")
    @ApiResponse(responseCode = "409", description = "insufficient-stock, or reservation-conflict (different lines)")
    @PutMapping("/{orderId}")
    public ResponseEntity<ReservationResponse> reserve(@PathVariable @Positive Long orderId,
                                                       @Valid @RequestBody ReservationRequest request) {
        ReservationService.Outcome outcome = reservationService.reserve(orderId, request);
        return ResponseEntity.status(outcome.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(outcome.reservation());
    }
}