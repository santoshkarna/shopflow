package com.shopflow.product.exception;

/**
 * A product with the given id does not exist.
 * Pure domain exception: no HTTP knowledge here; GlobalExceptionHandler maps it to 404.
 */
public class ProductNotFoundException extends RuntimeException {

    private final Long productId;

    public ProductNotFoundException(Long productId) {
        super("Product %d not found".formatted(productId));
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }
}