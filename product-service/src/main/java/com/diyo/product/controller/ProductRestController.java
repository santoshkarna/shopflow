package com.diyo.product.controller;


import com.diyo.product.dto.ProductRequest;
import com.diyo.product.dto.ProductResponse;
import com.diyo.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Product Service", description = "APIS for managing product info")
@RequiredArgsConstructor
@RestController
@RequestMapping("/products")
public class ProductRestController {

    private final ProductService productService;

    @Operation(
            summary = "Create a new product",
            description = "Creates a new product and returns the created products.",
            responses = {
                    @ApiResponse(
                            responseCode = "201", description = "Created",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ProductResponse.class)
                            )
                    )
            }
    )
    @PostMapping
    public ResponseEntity<ProductResponse> addProduct(@RequestBody @Valid ProductRequest product) {
        return new ResponseEntity<>(productService.addProduct(product), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Updates product",
            description = "Updates an existing product and returns the updated product.",
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Updated",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ProductResponse.class)
                            )
                    )
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@RequestBody @Valid ProductRequest product, @PathVariable Long id) {
        return ResponseEntity.ok(productService.updateProduct(product, id));
    }

    @Operation(
            summary = "Delete product",
            description = "Deletes an existing product.",
            responses = {
                    @ApiResponse(
                            responseCode = "204", description = "Deleted",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ProductResponse.class)
                            )
                    )
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "Get Product",
            description = "Retrieves an existing product by its ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ProductResponse.class)
                            )
                    )
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @Operation(
            summary = "Get All products",
            description = "Returns all products with detail",
            responses = {
                    @ApiResponse(
                            responseCode = "200", description = "Found",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))
                            )
                    )
            }
    )
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getProducts());
    }
}
