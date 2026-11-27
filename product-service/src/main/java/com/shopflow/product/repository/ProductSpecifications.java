package com.shopflow.product.repository;

import com.shopflow.product.domain.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Composable WHERE-clause fragments for product searches.
 * A specification returning null contributes nothing, so every filter is optional.
 * Attribute names are strings, checked when the query runs (see Failure 2).
 */
public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> inCategory(String categoryName) {
        return (root, query, cb) -> categoryName == null
                ? null
                : cb.equal(cb.lower(root.get("category").<String>get("name")),
                categoryName.toLowerCase(Locale.ROOT));
    }

    public static Specification<Product> priceAtLeast(BigDecimal minPrice) {
        return (root, query, cb) -> minPrice == null
                ? null
                : cb.greaterThanOrEqualTo(root.<BigDecimal>get("price"), minPrice);
    }

    public static Specification<Product> priceAtMost(BigDecimal maxPrice) {
        return (root, query, cb) -> maxPrice == null
                ? null
                : cb.lessThanOrEqualTo(root.<BigDecimal>get("price"), maxPrice);
    }

    public static Specification<Product> inStockOnly(boolean enabled) {
        return (root, query, cb) -> enabled
                ? cb.greaterThan(root.<Integer>get("stockQuantity"), 0)
                : null;
    }
}