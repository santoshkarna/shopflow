package com.diyo.product.service;

import com.diyo.product.dto.ProductRequest;
import com.diyo.product.dto.ProductResponse;
import com.diyo.product.exceptions.ApiException;
import com.diyo.product.mapper.ProductMapper;
import com.diyo.product.model.Product;
import com.diyo.product.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    public void testAddProduct() throws Exception {
        //Arrange
        ProductRequest productRequest = prepareProductRequest();
        Product product = ProductMapper.mapToProduct(productRequest);
        product.setId(1L);
        ProductResponse productResponseExpected = prepareProductResponse();

        //mock
        Mockito.when(productRepository.save(Mockito.any(Product.class))).thenReturn(product);

        //Act
        ProductResponse productResponseActual = productService.addProduct(productRequest);

        //Assert
        Assertions.assertNotNull(productResponseActual);
        Assertions.assertEquals(productResponseExpected, productResponseActual);
        Assertions.assertEquals(productResponseExpected.id(), productResponseActual.id());
        Assertions.assertEquals(productResponseExpected.name(), productResponseActual.name());
        Assertions.assertEquals(productResponseExpected.description(), productResponseActual.description());
        Assertions.assertEquals(productResponseExpected.price(), productResponseActual.price());
    }

    @Test
    public void testUpdateProduct() throws Exception {
        //Arrange
        ProductRequest productRequest = prepareProductRequest();
        Product product = ProductMapper.mapToProduct(productRequest);
        long productId = 1L;
        product.setId(productId);
        ProductResponse productResponseExpected = prepareProductResponse();

        //mock
        Mockito.when(productRepository.findById(productId)).thenReturn(java.util.Optional.of(product));
        Mockito.when(productRepository.save(Mockito.any(Product.class))).thenReturn(product);

        //Act
        ProductResponse productResponseActual = productService.updateProduct(productRequest, productId);

        //Assert
        Assertions.assertNotNull(productResponseActual);
        Assertions.assertEquals(productResponseExpected, productResponseActual);
        Assertions.assertEquals(productResponseExpected.id(), productResponseActual.id());
        Assertions.assertEquals(productResponseExpected.name(), productResponseActual.name());
        Assertions.assertEquals(productResponseExpected.description(), productResponseActual.description());
        Assertions.assertEquals(productResponseExpected.price(), productResponseActual.price());
    }

    @Test
    public void testUpdateProductWhenProductNotFound() throws Exception {
        //Arrange
        ProductRequest productRequest = prepareProductRequest();
        long productId = 1L;

        //mock
        Mockito.when(productRepository.findById(productId)).thenReturn(Optional.empty());

        //Act
        ApiException ex = Assertions.assertThrows(ApiException.class,
                () -> productService.updateProduct(productRequest, productId));

        //Assert
        Assertions.assertEquals("Product not found with id: " + productId, ex.getMessage());
        //verify
        Mockito.verify(productRepository, Mockito.times(1)).findById(productId);
        Mockito.verify(productRepository, Mockito.never()).save(Mockito.any(Product.class));
        Mockito.verifyNoMoreInteractions(productRepository);
    }

    @Test
    public void testDeleteProduct() throws Exception {
        //Arrange
        long productId = 1L;

        //mock
        Mockito.when(productRepository.existsById(productId)).thenReturn(Boolean.TRUE);
        Mockito.doNothing().when(productRepository).deleteById(productId);

        //Act
         productService.deleteProduct(productId);

        //verify
        Mockito.verify(productRepository, Mockito.times(1)).existsById(productId);
        Mockito.verify(productRepository, Mockito.times(1)).deleteById(productId);
        Mockito.verifyNoMoreInteractions(productRepository);
    }

    @Test
    public void testDeleteProductWhenProductNotFound() throws Exception {
        //Arrange
        long productId = 1L;

        //mock
        Mockito.when(productRepository.existsById(productId)).thenReturn(Boolean.FALSE);

        //Act
        ApiException ex = Assertions.assertThrows(ApiException.class,
                () -> productService.deleteProduct(productId));

        //Assert
        Assertions.assertEquals("Product not found with id: " + productId, ex.getMessage());
        //verify
        Mockito.verify(productRepository, Mockito.times(1)).existsById(productId);
        Mockito.verify(productRepository, Mockito.never()).deleteById(productId);
        Mockito.verifyNoMoreInteractions(productRepository);
    }



    @Test
    public void testGetProduct() throws Exception {
        //Arrange
        long productId = 1L;
        Product product = prepareProduct();
        ProductResponse productResponseExpected = prepareProductResponse();

        //mock
        Mockito.when(productRepository.findById(productId)).thenReturn(java.util.Optional.of(product));

        //Act
        ProductResponse productResponseActual = productService.getProduct(productId);

        //Assert
        Assertions.assertNotNull(productResponseActual);
        Assertions.assertEquals(productResponseExpected, productResponseActual);
        Assertions.assertEquals(productResponseExpected.id(), productResponseActual.id());
        Assertions.assertEquals(productResponseExpected.name(), productResponseActual.name());
        Assertions.assertEquals(productResponseExpected.description(), productResponseActual.description());
        Assertions.assertEquals(productResponseExpected.price(), productResponseActual.price());
    }

    @Test
    public void testGetProductWhenProductNotFound() throws Exception {
        //Arrange
        long productId = 1L;

        //mock
        Mockito.when(productRepository.findById(productId)).thenReturn(java.util.Optional.empty());

        //Act
        ApiException ex = Assertions.assertThrows(ApiException.class,
                () -> productService.getProduct(productId));

        //Assert
        Assertions.assertEquals("Product not found with id: " + productId, ex.getMessage());
        //verify
        Mockito.verify(productRepository, Mockito.times(1)).findById(productId);
        Mockito.verifyNoMoreInteractions(productRepository);
    }

    @Test
    public void testGetProducts() throws Exception {
        //Arrange
        Product product = prepareProduct();
        ProductResponse productResponseExpected = prepareProductResponse();

        //mock
        Mockito.when(productRepository.findAll()).thenReturn(List.of(product));

        //Act
        List<ProductResponse> productResponseActuals = productService.getProducts();

        //Assert
        Assertions.assertNotNull(productResponseActuals);
        ProductResponse productResponseActual = productResponseActuals.getFirst();
        Assertions.assertEquals(productResponseExpected, productResponseActual);
        Assertions.assertEquals(productResponseExpected.id(), productResponseActual.id());
        Assertions.assertEquals(productResponseExpected.name(), productResponseActual.name());
        Assertions.assertEquals(productResponseExpected.description(), productResponseActual.description());
        Assertions.assertEquals(productResponseExpected.price(), productResponseActual.price());
    }


    public ProductRequest prepareProductRequest() {
        return new ProductRequest(
                "Mobile",
                "This is Iphone 18 pro",
                300000.0
        );
    }

    public ProductResponse prepareProductResponse() {
        return new ProductResponse(
                1L,
                "Mobile",
                "This is Iphone 18 pro",
                300000.0
        );
    }

    public Product prepareProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Mobile");
        product.setDescription("This is Iphone 18 pro");
        product.setPrice(300000.0);
        return product;
    }
}
