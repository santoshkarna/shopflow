package com.diyo.product.service;

import com.diyo.product.dto.ProductRequest;
import com.diyo.product.dto.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse addProduct(ProductRequest productRequest);

    ProductResponse updateProduct(ProductRequest productRequest, Long id);

    void deleteProduct(Long id);

    ProductResponse getProduct(Long id);

    List<ProductResponse> getProducts();
}
