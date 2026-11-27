package com.shopflow.product.service;

import com.shopflow.product.domain.StockReservation;
import com.shopflow.product.dto.ReservationRequest;
import com.shopflow.product.dto.ReservationResponse;
import com.shopflow.product.exception.InsufficientStockException;
import com.shopflow.product.exception.ProductNotFoundException;
import com.shopflow.product.exception.ReservationConflictException;
import com.shopflow.product.repository.ProductRepository;
import com.shopflow.product.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Reserves stock for all lines of one order. ONE transaction: any short line rolls back every decrement.
 * Idempotent per orderId: the same lines again → existing reservation, no stock change;
 * different lines → ReservationConflictException.
 * Concurrent duplicates: the second insert hits uk_stock_reservations_order_product, its transaction rolls back
 * (its decrements too) → 409 data-conflict; a retry then sees the existing reservation → 200.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final StockReservationRepository reservationRepository;
    private final ProductRepository productRepository;

    /** created = true on the first successful call (201), false on an identical replay (200). */
    public record Outcome(ReservationResponse reservation, boolean created) {
    }

    @Transactional
    public Outcome reserve(Long orderId, ReservationRequest request) {
        SortedMap<Long, Integer> requested = request.lines().stream().collect(Collectors.toMap(
                ReservationRequest.Line::productId, ReservationRequest.Line::quantity, Integer::sum, TreeMap::new));

        List<StockReservation> existing = reservationRepository.findByOrderIdOrderByProductIdAsc(orderId);
        if (!existing.isEmpty()) {
            if (!quantitiesByProduct(existing).equals(requested)) {
                throw new ReservationConflictException(orderId);
            }
            log.debug("Reservation replay for order {}: nothing changed", orderId);
            return new Outcome(toResponse(orderId, existing), false);
        }

        List<StockReservation> created = new ArrayList<>();
        // TreeMap → ascending product id: every transaction locks product rows in the same order (no deadlocks).
        requested.forEach((productId, quantity) -> {
            int updated = productRepository.adjustStockAtomically(productId, -quantity);   // your L10 choice
            if (updated == 0) {
                int available = productRepository.findById(productId)
                        .orElseThrow(() -> new ProductNotFoundException(productId))
                        .getStockQuantity();
                throw new InsufficientStockException(productId, available, -quantity);   // rolls back ALL lines
            }
            created.add(reservationRepository.save(StockReservation.reserve(orderId, productId, quantity)));
        });

        log.info("Reserved stock for order {}: {}", orderId, requested);
        return new Outcome(toResponse(orderId, created), true);
    }

    /** order.events CONFIRMED: the held stock is sold. Repeats find nothing held → no-op. */
    @Transactional
    public int confirm(Long orderId) {
        List<StockReservation> held = held(orderId);
        held.forEach(StockReservation::confirm);
        log.info("Confirmed {} reservation(s) of order {}", held.size(), orderId);
        return held.size();
    }

    /** order.events PAYMENT_FAILED / CANCELLED: compensation — release and give the stock back. Repeats → no-op. */
    @Transactional
    public int release(Long orderId) {
        List<StockReservation> held = held(orderId);
        held.forEach(StockReservation::release);           // must come first: the bulk UPDATE below flushes these
        held.forEach(r -> productRepository.adjustStockAtomically(r.getProductId(), r.getQuantity()));  // then clears
        log.info("Released {} reservation(s) of order {}", held.size(), orderId);
        return held.size();
    }

    private List<StockReservation> held(Long orderId) {
        return reservationRepository.findByOrderIdOrderByProductIdAsc(orderId).stream()
                .filter(StockReservation::isHeld)
                .toList();
    }

    private static SortedMap<Long, Integer> quantitiesByProduct(List<StockReservation> reservations) {
        return reservations.stream().collect(Collectors.toMap(
                StockReservation::getProductId, StockReservation::getQuantity, Integer::sum, TreeMap::new));
    }

    private static ReservationResponse toResponse(Long orderId, List<StockReservation> reservations) {
        return new ReservationResponse(orderId, reservations.getFirst().getStatus().name(),
                reservations.stream().map(r -> new ReservationResponse.Line(r.getProductId(), r.getQuantity())).toList());
    }
}