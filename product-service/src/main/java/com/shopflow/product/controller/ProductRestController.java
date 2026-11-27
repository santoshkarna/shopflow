package com.shopflow.product.controller;

import com.shopflow.product.dto.ProductPatchRequest;
import com.shopflow.product.dto.ProductRequest;
import com.shopflow.product.dto.ProductResponse;
import com.shopflow.product.dto.StockAdjustmentRequest;
import com.shopflow.product.exception.ProductNotFoundException;
import com.shopflow.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import static com.shopflow.product.config.OpenApiConfig.PROBLEM_JSON;
import static com.shopflow.product.config.OpenApiConfig.PROBLEM_SCHEMA_REF;

/**
 * Product REST API: implements docs/api/product-api-v1.md, operations 1–7.
 * Errors are domain exceptions, mapped to RFC 9457 responses by GlobalExceptionHandler.
 * OpenAPI annotations document what the generator cannot infer (201, 404, 409); 400/500 come from OpenApiConfig.
 */
@Tag(name = "Products", description = "Catalog, inventory and search")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductRestController {

    private final ProductService productService;

    // 1. GET /api/v1/products?category=&minPrice=&maxPrice=&inStock=
    @Operation(summary = "List and filter products",
            description = "All filters are optional and combined with AND. Results are ordered by id.")
    @ApiResponse(responseCode = "200", description = "Matching products",
            headers = @Header(name = "X-Total-Count", description = "Number of products returned",
                    schema = @Schema(type = "integer")))
    @GetMapping
    public ResponseEntity<List<ProductResponse>> listProducts(
            @Parameter(description = "Category name, case-insensitive", example = "Laptops")
            @RequestParam(required = false) String category,
            @Parameter(description = "Minimum price (inclusive)", example = "60000")
            @RequestParam(required = false) @DecimalMin("0.00") BigDecimal minPrice,
            @Parameter(description = "Maximum price (inclusive)", example = "120000")
            @RequestParam(required = false) @DecimalMin("0.00") BigDecimal maxPrice,
            @Parameter(description = "Only products with stock > 0")
            @RequestParam(defaultValue = "false") boolean inStock) {
        List<ProductResponse> products = productService.search(category, minPrice, maxPrice, inStock);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(products.size()))
                .body(products);
    }

    // 2. GET /api/v1/products/{id}
    @Operation(summary = "Get one product")
    @ApiResponse(responseCode = "200", description = "The product")
    @ApiResponse(responseCode = "404", description = "No product with this id (type: product-not-found)",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(ref = PROBLEM_SCHEMA_REF)))
    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable @Positive Long id) {
        return productService.findById(id).orElseThrow(() -> notFound(id));
    }

    // 3. POST /api/v1/products → 201 + Location
    @Operation(summary = "Create a product",
            description = "The server assigns the id. A category that does not exist yet is created (case-insensitive match).")
    @ApiResponse(responseCode = "201", description = "Created",
            headers = @Header(name = "Location", description = "URI of the new product",
                    schema = @Schema(type = "string", format = "uri")))
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse created = productService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    // 4. PUT /api/v1/products/{id}
    @Operation(summary = "Replace a product", description = "Full replacement: every field is required. Never creates a product.")
    @ApiResponse(responseCode = "200", description = "The replaced product")
    @ApiResponse(responseCode = "404", description = "No product with this id (type: product-not-found)",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(ref = PROBLEM_SCHEMA_REF)))
    @PutMapping("/{id}")
    public ProductResponse replaceProduct(@PathVariable @Positive Long id,
                                          @Valid @RequestBody ProductRequest request) {
        return productService.replace(id, request).orElseThrow(() -> notFound(id));
    }

    // 5. PATCH /api/v1/products/{id}
    @Operation(summary = "Partially update a product",
            description = "Merge semantics: only fields present in the body change; absent or null fields stay unchanged.")
    @ApiResponse(responseCode = "200", description = "The updated product")
    @ApiResponse(responseCode = "404", description = "No product with this id (type: product-not-found)",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(ref = PROBLEM_SCHEMA_REF)))
    @PatchMapping("/{id}")
    public ProductResponse patchProduct(@PathVariable @Positive Long id,
                                        @Valid @RequestBody ProductPatchRequest patch) {
        return productService.patch(id, patch).orElseThrow(() -> notFound(id));
    }

    // 6. DELETE /api/v1/products/{id}
    @Operation(summary = "Delete a product")
    @ApiResponse(responseCode = "204", description = "Deleted (no body)")
    @ApiResponse(responseCode = "404", description = "No product with this id (type: product-not-found)",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(ref = PROBLEM_SCHEMA_REF)))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable @Positive Long id) {
        if (!productService.delete(id)) {
            throw notFound(id);
        }
    }

    // 7. POST /api/v1/products/{id}/stock-adjustments → 200; 404; 409
    @Operation(summary = "Adjust stock",
            description = "Adds (positive delta) or removes (negative delta) stock. NOT idempotent: never retry blindly.")
    @ApiResponse(responseCode = "200", description = "The product with its new stock")
    @ApiResponse(responseCode = "404", description = "No product with this id (type: product-not-found)",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(ref = PROBLEM_SCHEMA_REF)))
    @ApiResponse(responseCode = "409", description = "Stock would become negative (type: insufficient-stock; includes `available`)",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(ref = PROBLEM_SCHEMA_REF)))
    @PostMapping("/{id}/stock-adjustments")
    public ProductResponse adjustStock(@PathVariable @Positive Long id,
                                       @Valid @RequestBody StockAdjustmentRequest request) {
        return productService.adjustStock(id, request).orElseThrow(() -> notFound(id));
    }

    private static ProductNotFoundException notFound(Long id) {
        return new ProductNotFoundException(id);
    }
}