package com.streaker.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        mockRequest = mock(HttpServletRequest.class);
    }

    @Nested
    @DisplayName("handleNotFound()")
    class HandleNotFound {

        @Test
        @DisplayName("should return 404 with correct error response")
        void shouldReturn404() {
            when(mockRequest.getRequestURI()).thenReturn("/fake-uri");

            ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
            ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex, mockRequest);

            assertNotNull(response.getBody(), "Error response body should not be null");
            ErrorResponse body = response.getBody();

            assertAll(
                    () -> assertEquals(404, response.getStatusCode().value()),
                    () -> assertNotNull(body),
                    () -> assertEquals("Not Found", body.error()),
                    () -> assertEquals("Not found", body.message()),
                    () -> assertEquals("/fake-uri", body.path())
            );
        }
    }

    @Nested
    @DisplayName("handleBadRequest()")
    class HandleBadRequest {

        @Test
        @DisplayName("should return 400 with correct error response")
        void shouldReturn400() {
            when(mockRequest.getRequestURI()).thenReturn("/bad-uri");

            IllegalArgumentException ex = new IllegalArgumentException("Bad input");
            ResponseEntity<ErrorResponse> response = handler.handleBadRequest(ex, mockRequest);

            assertNotNull(response.getBody(), "Error response body should not be null");
            ErrorResponse body = response.getBody();

            assertAll(
                    () -> assertEquals(400, response.getStatusCode().value()),
                    () -> assertEquals("Bad Request", body.error()),
                    () -> assertEquals("Bad input", body.message()),
                    () -> assertEquals("/bad-uri", body.path())
            );
        }

    }

    @Nested
    @DisplayName("handleGeneric()")
    class HandleGeneric {

        @Test
        @DisplayName("should return 500 with correct error response")
        void shouldReturn500() {
            when(mockRequest.getRequestURI()).thenReturn("/crash");

            Exception ex = new Exception("Something failed");
            ResponseEntity<ErrorResponse> response = handler.handleGeneric(ex, mockRequest);

            assertNotNull(response.getBody(), "Error response body should not be null");
            ErrorResponse body = response.getBody();

            assertAll(
                    () -> assertEquals(500, response.getStatusCode().value()),
                    () -> assertNotNull(body),
                    () -> assertEquals("Internal Server Error", body.error()),
                    () -> assertEquals("Something failed", body.message()),
                    () -> assertEquals("/crash", body.path())
            );
        }
    }
}
