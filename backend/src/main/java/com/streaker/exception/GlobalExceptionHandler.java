package com.streaker.exception;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(request, HttpStatus.NOT_FOUND, "Not Found", ex);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        return buildErrorResponse(request, HttpStatus.BAD_REQUEST, "Bad Request", ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errorMsg = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return buildErrorResponse(request, HttpStatus.BAD_REQUEST, "Validation Failed", errorMsg, ex);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest request) {
        return buildErrorResponse(request, HttpStatus.BAD_REQUEST, "Missing Header", ex);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(UnauthorizedAccessException ex, HttpServletRequest request) {
        return buildErrorResponse(request, HttpStatus.FORBIDDEN, "Forbidden", ex);
    }

    @ExceptionHandler({JwtException.class, SecurityException.class})
    public ResponseEntity<ErrorResponse> handleJwtAndSecurity(Exception ex, HttpServletRequest request) {
        return buildErrorResponse(request, HttpStatus.UNAUTHORIZED, "Unauthorized", ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return buildErrorResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex);
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ErrorResponse> handleCustomRateLimit(TooManyRequestsException ex, HttpServletRequest request) {
        return buildErrorResponse(request, HttpStatus.TOO_MANY_REQUESTS, "Too many login attempts, please try later", ex);
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ErrorResponse> handleRateLimiter(RequestNotPermitted ex, HttpServletRequest request) {
        return buildErrorResponse(request, HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded", ex);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpServletRequest request, HttpStatus status, String error, Exception ex) {
        logError(request, error, ex);
        return ResponseEntity.status(status).body(new ErrorResponse(
                Instant.now(),
                status.value(),
                error,
                ex.getMessage(),
                request.getRequestURI()
        ));
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpServletRequest request, HttpStatus status, String error, String message, Exception ex) {
        logError(request, error, ex);
        return ResponseEntity.status(status).body(new ErrorResponse(
                Instant.now(),
                status.value(),
                error,
                message,
                request.getRequestURI()
        ));
    }

    private void logError(HttpServletRequest request, String label, Exception ex) {
        log.error("{} at [{} {}] from {}: {}",
                label,
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                ex.getMessage(),
                ex
        );
    }
}
