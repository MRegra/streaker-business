package com.streaker.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final HttpServletRequest mockRequest = mock(HttpServletRequest.class);

    @Test
    void handleNotFound_shouldReturn404() {
        when(mockRequest.getRequestURI()).thenReturn("/fake-uri");

        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex, mockRequest);

        assertEquals(404, response.getStatusCode().value());
        Assertions.assertNotNull(response.getBody());
        assertEquals("Not Found", response.getBody().error());
        assertEquals("Not found", response.getBody().message());
        assertEquals("/fake-uri", response.getBody().path());
    }

    @Test
    void handleBadRequest_shouldReturn400() {
        when(mockRequest.getRequestURI()).thenReturn("/bad-uri");

        IllegalArgumentException ex = new IllegalArgumentException("Bad input");
        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(ex, mockRequest);

        assertEquals(400, response.getStatusCode().value());
        Assertions.assertNotNull(response.getBody());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("Bad input", response.getBody().message());
        assertEquals("/bad-uri", response.getBody().path());
    }

    @Test
    void handleGeneric_shouldReturn500() {
        when(mockRequest.getRequestURI()).thenReturn("/crash");

        Exception ex = new Exception("Something failed");
        ResponseEntity<ErrorResponse> response = handler.handleGeneric(ex, mockRequest);

        assertEquals(500, response.getStatusCode().value());
        Assertions.assertNotNull(response.getBody());
        assertEquals("Internal Server Error", response.getBody().error());
        assertEquals("Something failed", response.getBody().message());
        assertEquals("/crash", response.getBody().path());
    }
}
