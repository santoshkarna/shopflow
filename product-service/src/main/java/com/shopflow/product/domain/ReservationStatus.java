package com.shopflow.product.domain;

/** Lifecycle of stock held for an order. CONFIRMED / RELEASED / EXPIRED arrive with order events (L36). */
public enum ReservationStatus {
    RESERVED,
    CONFIRMED,
    RELEASED,
    EXPIRED
}