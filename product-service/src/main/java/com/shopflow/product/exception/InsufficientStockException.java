package com.shopflow.product.exception;
/**
 * A stock adjustment would make stock negative.
 * Unchecked, so @Transactional rolls back (L10). Mapped to 409 by GlobalExceptionHandler.
 * Carries the numbers so the error response can tell the client what IS possible.
 */
public class InsufficientStockException extends RuntimeException {

    private final Long productId;
    private final int available;
    private final int requestedChange;

    public InsufficientStockException(Long productId, int available, int requestedChange) {
        super("Product %d has %d in stock; adjustment of %d would make it negative"
                .formatted(productId, available, requestedChange));
        this.productId = productId;
        this.available = available;
        this.requestedChange = requestedChange;
    }

    public Long getProductId() {
        return productId;
    }

    public int getAvailable() {
        return available;
    }

    public int getRequestedChange() {
        return requestedChange;
    }
}