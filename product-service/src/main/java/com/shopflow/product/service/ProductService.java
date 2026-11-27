package com.shopflow.product.service;

import com.shopflow.product.domain.Category;
import com.shopflow.product.domain.Product;
import com.shopflow.product.dto.ProductPatchRequest;
import com.shopflow.product.dto.ProductRequest;
import com.shopflow.product.dto.ProductResponse;
import com.shopflow.product.dto.StockAdjustmentRequest;
import com.shopflow.product.exception.InsufficientStockException;
import com.shopflow.product.mapper.ProductMapper;
import com.shopflow.product.repository.CategoryRepository;
import com.shopflow.product.repository.ProductRepository;
import com.shopflow.product.repository.ProductSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for the product catalog. Public API uses DTOs only.
 * Transactions: read-only by default (class level); every method that writes overrides with @Transactional.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private static final Sort BY_ID = Sort.by("id");

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public List<ProductResponse> findAll() {
        return productRepository.findAll(BY_ID).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    /**
     * Contract op 1. All filters run in ONE SQL statement:
     * absent filters contribute nothing; the category is fetched in the same query (@EntityGraph).
     */
    public List<ProductResponse> search(String category, BigDecimal minPrice, BigDecimal maxPrice,
                                        boolean inStockOnly) {
        Specification<Product> filters = Specification.allOf(
                ProductSpecifications.inCategory(category),
                ProductSpecifications.priceAtLeast(minPrice),
                ProductSpecifications.priceAtMost(maxPrice),
                ProductSpecifications.inStockOnly(inStockOnly));

        return productRepository.findAll(filters, BY_ID).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    /** Contract op 2. */
    public Optional<ProductResponse> findById(Long id) {
        return productRepository.findById(id).map(productMapper::toResponse);
    }

    /** Contract op 3. save() IS needed here: a new object is not yet managed by Hibernate. */
    @Transactional
    public ProductResponse create(ProductRequest request) {
        Category category = resolveCategory(request.category());
        Product saved = productRepository.save(productMapper.toNewProduct(request, category));
        log.info("Created product {} '{}' in category '{}'", saved.getId(), saved.getName(), category.getName());
        return productMapper.toResponse(saved);
    }

    /** Contract op 4. No save(): 'existing' is managed, so dirty checking writes the UPDATE at commit. */
    @Transactional
    public Optional<ProductResponse> replace(Long id, ProductRequest request) {
        return productRepository.findById(id).map(existing -> {
            productMapper.replaceFields(request, resolveCategory(request.category()), existing);
            return productMapper.toResponse(existing);
        });
    }

    /** Contract op 5. No save(): dirty checking. */
    @Transactional
    public Optional<ProductResponse> patch(Long id, ProductPatchRequest patch) {
        return productRepository.findById(id).map(existing -> {
            Category newCategory = (patch.category() == null) ? null : resolveCategory(patch.category());
            productMapper.applyPatch(patch, newCategory, existing);
            log.info("Patched product {}", id);
            return productMapper.toResponse(existing);
        });
    }

    /** Contract op 6. */
    @Transactional
    public boolean delete(Long id) {
        if (!productRepository.existsById(id)) {
            return false;
        }
        productRepository.deleteById(id);
        log.info("Deleted product {}", id);
        return true;
    }

    /**
     * Contract op 7. Not idempotent: each call applies its delta again.
     * The row lock serializes concurrent adjustments of the same product (no lost updates).
     * InsufficientStockException (unchecked) rolls the transaction back and releases the lock.
     */
    @Transactional
    public Optional<ProductResponse> adjustStock(Long id, StockAdjustmentRequest request) {
        int updated = productRepository.adjustStockAtomically(id, request.delta());
        if (updated == 0) {
            Optional<Product> current = productRepository.findById(id);
            if (current.isEmpty()) {
                return Optional.empty();                                          // → 404
            }
            throw new InsufficientStockException(id, current.get().getStockQuantity(), request.delta());  // → 409
        }
        log.info("Stock of product {} adjusted by {} ({})", id, request.delta(), request.reason());
        return productRepository.findById(id).map(productMapper::toResponse);
    }

    public long countProducts() {
        return productRepository.count();
    }

    /**
     * Find the category by name (case-insensitive) or create it.
     * Known limitation: two concurrent requests creating the same NEW category can both try to insert;
     * the unique constraint rejects the second (500 until L14 maps it).
     */
    private Category resolveCategory(String name) {
        if (name == null) {
            return null;  // TEMPORARY: L13 validation rejects a missing category with 400
        }
        String trimmed = name.trim();
        return categoryRepository.findByNameIgnoreCase(trimmed)
                .orElseGet(() -> {
                    log.info("Created category '{}'", trimmed);
                    return categoryRepository.save(new Category(trimmed));
                });
    }
}