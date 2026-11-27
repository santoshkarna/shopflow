package com.shopflow.product.repository;

import com.shopflow.product.domain.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {

    List<StockReservation> findByOrderIdOrderByProductIdAsc(Long orderId);
}