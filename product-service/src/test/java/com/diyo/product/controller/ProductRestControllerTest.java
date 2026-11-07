package com.diyo.product.controller;


import com.diyo.product.dto.ProductRequest;
import com.diyo.product.dto.ProductResponse;
import com.diyo.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ProductRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    private ObjectMapper objectMapper;

    private ProductRequest productRequest;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        //Initialize MockMvc with the controller and mock service
        this.mockMvc = MockMvcBuilders.standaloneSetup(new ProductRestController(productService)).build();
        this.objectMapper = new ObjectMapper();
//        objectMapper.registerModule(new JavaTimeModule());

        this.productRequest = new ProductRequest(
                "Iphone",
                "This is Iphone 18 Pro",
                30000.0
        );

        this.productResponse = new ProductResponse(
                1L,
                "Iphone",
                "This is Iphone 18 Pro",
                30000.0
        );
    }

    //post method(add product)
    @Test
    void testAddProduct_WithValidRequest_ReturnsCreated() throws Exception {
        //Arrange
        Mockito.when(productService.addProduct(productRequest)).thenReturn(productResponse);

        //Act & Assert
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Iphone"))
                .andExpect(jsonPath("$.description").value("This is Iphone 18 Pro"))
                .andExpect(jsonPath("$.price").value(30000.0));

        Mockito.verify(productService, Mockito.times(1)).addProduct(Mockito.any(ProductRequest.class));
    }

    @Test
    void testAddProduct_WithInValidName_ReturnsBadRequest() throws Exception {
        //Arrange
        ProductRequest invalidProductRequest = new ProductRequest(
                "",
                "Hello wrong request",
                20000.0
        );

        //Act & Assert
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProductRequest)))
                .andExpect(status().isBadRequest());

        Mockito.verify(productService, Mockito.never()).addProduct(Mockito.any(ProductRequest.class));
    }

    //put method(add product)
    @Test
    void testUpdateProduct_WithValidRequest_ReturnsOk() throws Exception {
        //Arrange
        long productId = 1L;
        Mockito.when(productService.updateProduct(productRequest, productId)).thenReturn(productResponse);

        //Act & Assert
        mockMvc.perform(put("/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Iphone"))
                .andExpect(jsonPath("$.description").value("This is Iphone 18 Pro"))
                .andExpect(jsonPath("$.price").value(30000.0));

        Mockito.verify(productService, Mockito.times(1)).updateProduct(Mockito.any(ProductRequest.class), eq(productId));
    }

    @Test
    void testUpdateProduct_WithInValidName_ReturnsBadRequest() throws Exception {
        //Arrange
        Long productId = 1L;
        ProductRequest invalidProductRequest = new ProductRequest(
                "",
                "Hello wrong request",
                20000.0
        );

        //Act & Assert
        mockMvc.perform(put("/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProductRequest)))
                .andExpect(status().isBadRequest());

        Mockito.verify(productService, Mockito.never()).updateProduct(Mockito.any(ProductRequest.class), eq(productId));
    }

    //delete method
    @Test
    void testDeleteProduct_WithValidRequest_ReturnsNoContent() throws Exception {
        long productId = 1L;

        Mockito.doNothing().when(productService).deleteProduct(productId);

        mockMvc.perform(delete("/products/{id}", productId))
                .andExpect(status().isNoContent());

        Mockito.verify(productService, Mockito.times(1)).deleteProduct(productId);
    }

    //get method
    @Test
    void testGetProduct_WithValidId_ReturnsOk() throws Exception {
        long productId = 1L;
        Mockito.when(productService.getProduct(productId)).thenReturn(productResponse);

        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Iphone"))
                .andExpect(jsonPath("$.description").value("This is Iphone 18 Pro"))
                .andExpect(jsonPath("$.price").value(30000.0));

        Mockito.verify(productService, Mockito.times(1)).getProduct(productId);
    }

    //Get All
    @Test
    void testGetAllProducts_ReturnsOk() throws Exception {
        Mockito.when(productService.getProducts()).thenReturn(List.of(productResponse));

        mockMvc.perform(get("/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Iphone"))
                .andExpect(jsonPath("$[0].description").value("This is Iphone 18 Pro"))
                .andExpect(jsonPath("$[0].price").value(30000.0));

        Mockito.verify(productService, Mockito.times(1)).getProducts();
    }

    @Test
    void testGetAllProducts_WithEmptyList_ReturnsOkWithEmptyArray() throws Exception {
        Mockito.when(productService.getProducts()).thenReturn(List.of());

        mockMvc.perform(get("/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        Mockito.verify(productService, Mockito.times(1)).getProducts();
    }

}
