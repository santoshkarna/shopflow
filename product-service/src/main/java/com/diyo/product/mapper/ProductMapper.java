package com.diyo.product.mapper;

import com.diyo.product.dto.ProductRequest;
import com.diyo.product.dto.ProductResponse;
import com.diyo.product.model.Product;

public class ProductMapper {

    public static Product mapToProduct(final ProductRequest request) {
        final Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        return product;
    }

    public static ProductResponse mapToProductResponse(final Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice()
        );
    }

    public static Product mapToProductForUpdate(final Product product, final ProductRequest productRequest) {
        product.setName(productRequest.name());
        product.setDescription(productRequest.description());
        product.setPrice(productRequest.price());
        return product;
    }
}
