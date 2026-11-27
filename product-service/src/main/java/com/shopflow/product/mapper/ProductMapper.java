package com.shopflow.product.mapper;

import com.shopflow.product.domain.Category;
import com.shopflow.product.dto.ProductPatchRequest;
import com.shopflow.product.dto.ProductRequest;
import com.shopflow.product.dto.ProductResponse;
import com.shopflow.product.domain.Product;


import org.springframework.stereotype.Component;

/**
 * The single place where API types and the Product entity are converted.
 * Category lookup/creation is the service's job; the mapper only receives the resolved Category.
 */
@Component
public class ProductMapper {

    /** POST: build a new entity. The id stays null; the database assigns it. */
    public Product toNewProduct(ProductRequest request, Category category) {
        Product product = new Product();
        copyAllFields(request, category, product);
        return product;
    }

    /** PUT: overwrite every client-settable field of an existing entity. */
    public void replaceFields(ProductRequest request, Category category, Product target) {
        copyAllFields(request, category, target);
    }

    /** PATCH: overwrite only provided fields. newCategory is null when the patch does not change it. */
    public void applyPatch(ProductPatchRequest patch, Category newCategory, Product target) {
        if (patch.name() != null) {
            target.setName(patch.name());
        }
        if (newCategory != null) {
            target.setCategory(newCategory);
        }
        if (patch.price() != null) {
            target.setPrice(patch.price());
        }
        if (patch.stockQuantity() != null) {
            target.setStockQuantity(patch.stockQuantity());
        }
    }

    /** Must be called inside a transaction: reading the lazy category may query the database. */
    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getCategory().getName(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStockQuantity() > 0);
    }

    private void copyAllFields(ProductRequest request, Category category, Product target) {
        // Unbox first: a missing stockQuantity fails before any field changes.
        // TEMPORARY: L13 validation rejects missing fields with 400 before this code runs.
        int stockQuantity = request.stockQuantity();

        target.setName(request.name());
        target.setCategory(category);
        target.setPrice(request.price());
        target.setStockQuantity(stockQuantity);
    }
}
