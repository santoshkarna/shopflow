package com.shopflow.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;

/**
 * Units of one product held for one order (aggregate in the Catalog & Inventory context, L25).
 * Created only through reserve(...); status changes get intent methods in L36. No setters.
 */
@Entity
@Table(name = "stock_reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // for JPA only
@ToString
public class StockReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;             // Ordering context, by id only

    @Column(nullable = false)
    private Long productId;           // same context; FK in V3, plain id here keeps the aggregate small

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    public static StockReservation reserve(Long orderId, Long productId, int quantity) {
        StockReservation reservation = new StockReservation();
        reservation.orderId = orderId;
        reservation.productId = productId;
        reservation.quantity = quantity;
        reservation.status = ReservationStatus.RESERVED;
        reservation.createdAt = Instant.now();
        return reservation;
    }

    /** Still holding stock for its order (the only state that confirm/release act on: idempotent consumers). */
    public boolean isHeld() {
        return status == ReservationStatus.RESERVED;
    }

    public void confirm() {
        requireHeld();
        status = ReservationStatus.CONFIRMED;
    }

    public void release() {
        requireHeld();
        status = ReservationStatus.RELEASED;
    }

    private void requireHeld() {
        if (status != ReservationStatus.RESERVED) {
            throw new IllegalStateException("Reservation " + id + " is " + status + ", not RESERVED");
        }
    }
}
