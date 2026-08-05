package com.suraj.rag.documentprocessing.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.LinkedHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String INTERNAL_ERROR_DETAIL = "An unexpected error occurred while processing the request";

    @ExceptionHandler(DocumentNotFoundException.class)
    ProblemDetail handleNotFound(DocumentNotFoundException ex, HttpServletRequest request) {
        log.warn("Document not found path={} message={}", request.getRequestURI(), ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Document not found", ex.getMessage(), request);
    }

    @ExceptionHandler(ValidationException.class)
    ProblemDetail handleValidation(ValidationException ex, HttpServletRequest request) {
        log.warn("Request validation failed path={} message={}", request.getRequestURI(), ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "Validation failed", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleMethodArgumentValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Request body validation failed path={} errorCount={}", request.getRequestURI(), ex.getErrorCount());
        var problem = problem(HttpStatus.BAD_REQUEST, "Validation failed", "Request body validation failed", request);
        var errors = new LinkedHashMap<String, String>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(DocumentProcessingException.class)
    ProblemDetail handleProcessing(DocumentProcessingException ex, HttpServletRequest request) {
        log.warn("Document processing exception path={} message={}", request.getRequestURI(), ex.getMessage(), ex);
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Document processing failed", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception path={}", request.getRequestURI(), ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", INTERNAL_ERROR_DETAIL, request);
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail, HttpServletRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }
}
