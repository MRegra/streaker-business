package com.streaker.exception;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.error("Not Found exception at {} {} from {}: {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                ex.getMessage(),
                ex);
        return new ResponseEntity<>(new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        ), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        log.error("Bad Request exception at {} {} from {}: {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                ex.getMessage(),
                ex);
        return new ResponseEntity<>(new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        ), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception occurred at {} {} from {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                ex);
        return new ResponseEntity<>(new ErrorResponse(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage(),
                request.getRequestURI()
        ), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error("Validation exception occurred at {} {} from {}: {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                ex.getMessage(),
                ex);
        String errorMsg = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                errorMsg,
                request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(
            UnauthorizedAccessException ex, HttpServletRequest request) {
        log.warn("Unauthorized access at {} {} from {}: {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                ex.getMessage(),
                ex);


        ErrorResponse error = new ErrorResponse(
                Instant.now(),
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<String> handleJwtException(JwtException ex, HttpServletRequest request) {
        log.warn("Unauthorized access at {} {} from {}: {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                ex.getMessage(),
                ex);

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid or malformed JWT: " + ex.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<String> handleSecurityException(SecurityException ex, HttpServletRequest request) {
        log.warn("Unauthorized access at {} {} from {}: {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                ex.getMessage(),
                ex);

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Unauthorized access: " + ex.getMessage());
    }
}
