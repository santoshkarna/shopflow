package com.shopflow.product.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI document settings shared by all operations.
 * Error responses follow RFC 9457 (L14); the "Problem" schema describes them once.
 */
@Configuration
public class OpenApiConfig {

    public static final String PROBLEM_SCHEMA_REF = "#/components/schemas/Problem";
    public static final String PROBLEM_JSON = "application/problem+json";

    @Bean
    public OpenAPI shopflowProductOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ShopFlow Product API")
                        .version("v1")
                        .description("""
                                Catalog, inventory and search for the ShopFlow e-commerce platform.
                                All errors are RFC 9457 Problem Details (application/problem+json);
                                clients must branch on the `type` field. Human-readable contract: docs/api/product-api-v1.md.""")
                        .contact(new Contact().name("ShopFlow Catalog Team").email("catalog-team@shopflow.example")))
                .components(new Components().addSchemas("Problem", problemSchema()));
    }

    /** Every operation can fail validation (400) or hit an unexpected fault (500): document both once, everywhere. */
    @Bean
    public OpenApiCustomizer standardProblemResponses() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation -> {
                    ApiResponses responses = operation.getResponses();
                    if (responses == null) {
                        return;
                    }
                    if (!responses.containsKey("400")) {
                        responses.addApiResponse("400", problemResponse("Invalid request (validation error or malformed input)"));
                    }
                    if (!responses.containsKey("500")) {
                        responses.addApiResponse("500", problemResponse("Unexpected server error (details only in server logs)"));
                    }
                }));
    }

    private static ApiResponse problemResponse(String description) {
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(PROBLEM_JSON,
                        new MediaType().schema(new Schema<>().$ref(PROBLEM_SCHEMA_REF))));
    }

    private static Schema<?> problemSchema() {
        return new ObjectSchema()
                .description("RFC 9457 Problem Details. Extension members depend on `type`, "
                        + "e.g. productId, available, requestedChange, errors[{field, message}].")
                .addProperty("type", new StringSchema().format("uri")
                        .example("https://shopflow.example/problems/product-not-found"))
                .addProperty("title", new StringSchema().example("Product not found"))
                .addProperty("status", new IntegerSchema().example(404))
                .addProperty("detail", new StringSchema().example("Product 999 not found"))
                .addProperty("instance", new StringSchema().format("uri-reference").example("/api/v1/products/999"))
                .additionalProperties(true);
    }
}