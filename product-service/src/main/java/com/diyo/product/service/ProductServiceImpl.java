package com.diyo.product.service;

import com.diyo.product.dto.ProductRequest;
import com.diyo.product.dto.ProductResponse;
import com.diyo.product.exceptions.ApiException;
import com.diyo.product.mapper.ProductMapper;
import com.diyo.product.model.Product;
import com.diyo.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

//    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;

    @Override
    public ProductResponse addProduct(ProductRequest productRequest) {
        log.info("Adding product to repository {}", productRequest);
        Product product = ProductMapper.mapToProduct(productRequest);
        Product savedProduct = productRepository.save(product);
        log.info("Product added successfully, id {}", savedProduct.getId());
        return ProductMapper.mapToProductResponse(savedProduct);
    }

    @Override
    public ProductResponse updateProduct(ProductRequest productRequest, Long id) {
        log.info("Updating product with id {}", id);
        Optional<Product> product = productRepository.findById(id);
        if (product.isEmpty()) {
            log.error("Product with id {} not found", id);
            throw new ApiException("Product not found with id: " + id, HttpStatus.NOT_FOUND);
        }
        ProductMapper.mapToProductForUpdate(product.get(), productRequest);
        Product updatedProduct = productRepository.save(product.get());
        log.info("Product updated successfully, id {}", updatedProduct.getId());
        return ProductMapper.mapToProductResponse(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        log.info("Deleting product with id {}", id);
        boolean exists = productRepository.existsById(id);
        if (!exists) {
            log.error("Product with id {} not found", id);
            throw new ApiException("Product not found with id: " + id, HttpStatus.NOT_FOUND);
        }
        productRepository.deleteById(id);
        log.info("Product deleted successfully, id {}", id);
    }

    @Override
    public ProductResponse getProduct(Long id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isEmpty()) {
            log.error("Product with id {} not found", id);
            throw new ApiException("Product not found with id: " + id, HttpStatus.NOT_FOUND);
        }
        log.info("Product found with id {}", id);
        return ProductMapper.mapToProductResponse(product.get());
    }

    @Override
    public List<ProductResponse> getProducts() {
        log.info("Retrieving all products");
        List<Product> products = productRepository.findAll();
        return products.stream().
                map(ProductMapper::mapToProductResponse)
                .collect(Collectors.toList());
    }

}
