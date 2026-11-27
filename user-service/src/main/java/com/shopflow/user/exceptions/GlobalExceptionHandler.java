package com.shopflow.user.exceptions;

import com.shopflow.user.auth.InvalidCredentialsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    private static final String PROBLEM_BASE = "";

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException ex){
        log.debug("User not found: {}", ex.getUserId());
        ProblemDetail problem = problem(HttpStatus.NOT_FOUND, "user-not-found", "User not found", ex.getMessage());
        problem.setProperty("userId", ex.getUserId());
        return problem;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredential(InvalidCredentialsException ex){
        log.info("Login rejected");
        return problem(HttpStatus.UNAUTHORIZED, "invalid-credentials", "Invalid credentials", ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleInvalidCredential(DataIntegrityViolationException ex){
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return problem(HttpStatus.CONFLICT, "data-conflict", "Conflict data",
                "The request conflicts with existing data (for example a duplicate value). Retry or change the request.");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex){
        log.error("Unhandled exception", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error", "Internal server error",
                "An unexpected error occurred. Please try again later.");
    }

    private static ProblemDetail problem(HttpStatus status, String type, String title, String detail){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(PROBLEM_BASE + type));
        problem.setTitle(title);
        return problem;
    }
}
