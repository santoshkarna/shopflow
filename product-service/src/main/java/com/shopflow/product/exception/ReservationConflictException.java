package com.shopflow.product.exception;

import lombok.Getter;

/** The order already holds a reservation with DIFFERENT lines. Becomes 409 reservation-conflict. */
@Getter
public class ReservationConflictException extends RuntimeException {

    private final Long orderId;

    public ReservationConflictException(Long orderId) {
        super("Order %d already has a different stock reservation".formatted(orderId));
        this.orderId = orderId;
    }
}