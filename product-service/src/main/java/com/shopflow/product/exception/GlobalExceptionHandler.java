package com.shopflow.product.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The ONE place where exceptions become HTTP responses.
 * Every error body is RFC 9457 Problem Details (application/problem+json) with a stable "type".
 *
 * Inherited from ResponseEntityExceptionHandler: all Spring MVC exceptions (405, 406, 415, malformed JSON,
 * type mismatches, …) already as ProblemDetail. Overridden here: validation (field list) and unknown routes.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    static final String PROBLEM_BASE = "https://shopflow.example/problems/";

    /** One invalid field or parameter, as returned in the "errors" list. */
    public record FieldViolation(String field, String message) {
    }

    // ---- domain exceptions ---------------------------------------------------------------------

    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handleProductNotFound(ProductNotFoundException ex) {
        log.debug("Product not found: {}", ex.getProductId());                       // NEW: client-caused, high volume
        ProblemDetail problem = problem(HttpStatus.NOT_FOUND, "product-not-found", "Product not found", ex.getMessage());
        problem.setProperty("productId", ex.getProductId());
        return problem;
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ProblemDetail handleInsufficientStock(InsufficientStockException ex) {
        log.info("Rejected stock adjustment: {}", ex.getMessage());                  // NEW: normal business rejection
        ProblemDetail problem = problem(HttpStatus.CONFLICT, "insufficient-stock", "Insufficient stock", ex.getMessage());
        problem.setProperty("productId", ex.getProductId());
        problem.setProperty("available", ex.getAvailable());
        problem.setProperty("requestedChange", ex.getRequestedChange());
        return problem;
    }

    @ExceptionHandler(ReservationConflictException.class)
    public ProblemDetail handleReservationConflict(ReservationConflictException ex) {
        log.info("Rejected reservation: {}", ex.getMessage());                       // business rejection
        ProblemDetail problem = problem(HttpStatus.CONFLICT, "reservation-conflict",
                "Conflicting reservation", ex.getMessage());
        problem.setProperty("orderId", ex.getOrderId());
        return problem;
    }

    // ---- database ------------------------------------------------------------------------------

    /** E.g. two concurrent requests creating the same new category (L09 known limitation). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());   // detail for operators
        return problem(HttpStatus.CONFLICT, "data-conflict", "Conflicting data",
                "The request conflicts with existing data (for example a duplicate value). Retry or change the request.");
    }

    // ---- anything unexpected -------------------------------------------------------------------

    /** Last resort: log everything, reveal nothing. */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);                                                // full stack trace in the log
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error", "Internal server error",
                "An unexpected error occurred. Please try again later.");
    }

    // ---- Spring MVC exceptions we enrich -------------------------------------------------------

    /** Invalid @Valid @RequestBody: list every invalid field. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        List<FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldViolation(error.getField(), error.getDefaultMessage()))
                .toList();
        return handleExceptionInternal(ex, validationProblem(violations), headers, status, request);
    }

    /** Invalid constrained parameters (@Positive id, @DecimalMin minPrice), validated by Spring MVC itself. */
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        List<FieldViolation> violations = new ArrayList<>();
        for (ParameterValidationResult result : ex.getParameterValidationResults()) {
            if (result instanceof ParameterErrors errors) {              // an object argument (e.g. a body)
                errors.getFieldErrors().forEach(error ->
                        violations.add(new FieldViolation(error.getField(), error.getDefaultMessage())));
            } else {                                                     // a simple parameter (id, minPrice)
                String parameter = result.getMethodParameter().getParameterName();
                result.getResolvableErrors().forEach(error ->
                        violations.add(new FieldViolation(parameter, error.getDefaultMessage())));
            }
        }
        return handleExceptionInternal(ex, validationProblem(violations), headers, status, request);
    }

    /** No controller matches the URL: make it distinguishable from "product not found". */
    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        ProblemDetail problem = problem(HttpStatus.NOT_FOUND, "route-not-found", "Route not found",
                "No endpoint matches %s /%s".formatted(ex.getHttpMethod(), ex.getResourcePath()));
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    // ---- helpers -------------------------------------------------------------------------------

    private static ProblemDetail validationProblem(List<FieldViolation> violations) {
        List<FieldViolation> sorted = violations.stream()
                .sorted(Comparator.comparing(FieldViolation::field).thenComparing(FieldViolation::message))
                .toList();
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "validation-error", "Invalid request",
                "%d validation error(s)".formatted(sorted.size()));
        problem.setProperty("errors", sorted);
        return problem;
    }

    private static ProblemDetail problem(HttpStatus status, String type, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(PROBLEM_BASE + type));
        problem.setTitle(title);
        return problem;
    }
}