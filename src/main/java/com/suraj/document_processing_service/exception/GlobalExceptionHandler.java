package com.suraj.document_processing_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DocumentNotFoundException.class)
    ProblemDetail handleNotFound(DocumentNotFoundException ex, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "Document not found", ex.getMessage(), request);
    }

    @ExceptionHandler({ValidationException.class, MethodArgumentNotValidException.class})
    ProblemDetail handleValidation(Exception ex, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Validation failed", ex.getMessage(), request);
    }

    @ExceptionHandler(DocumentProcessingException.class)
    ProblemDetail handleProcessing(DocumentProcessingException ex, HttpServletRequest request) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Document processing failed", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", ex.getMessage(), request);
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail, HttpServletRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }
}
