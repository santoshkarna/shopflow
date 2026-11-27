package com.shopflow.product.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The value may be absent (null), but if present it must contain non-whitespace characters.
 * Used for optional text fields such as PATCH requests.
 */
@Documented
@Constraint(validatedBy = NullOrNotBlankValidator.class)
@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
public @interface NullOrNotBlank {

    String message() default "must be absent or contain at least one non-whitespace character";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}