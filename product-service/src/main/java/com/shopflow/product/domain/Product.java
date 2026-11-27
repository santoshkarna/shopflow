package com.shopflow.product.domain;

import com.shopflow.product.exception.InsufficientStockException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products",
        indexes = @Index(name = "idx_products_category_id", columnList = "category_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    @ToString.Exclude                       // never trigger a lazy load (or recursion) from toString
    private Category category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;               // NUMERIC(12,2): exact decimal storage for money

    @Column(nullable = false)
    private int stockQuantity;              // column name: stock_quantity (naming strategy)

    /**
     * Domain rule: stock can never become negative.
     * Used by the pessimistic-lock strategy; the atomic-UPDATE strategy (your choice) enforces the
     * same rule in SQL. Kept here as the single readable statement of the rule.
     */
    public void adjustStock(int delta) {
        int newQuantity = stockQuantity + delta;
        if (newQuantity < 0) {
            throw new InsufficientStockException(id, stockQuantity, delta);
        }
        stockQuantity = newQuantity;
    }
}
