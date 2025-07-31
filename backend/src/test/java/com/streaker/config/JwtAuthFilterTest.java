package com.streaker.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("JwtAuthFilter Tests")
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext(); // Ensure clean context
    }

    @Nested
    @DisplayName("doFilterInternal")
    class DoFilterInternal {

        @Test
        @DisplayName("should set authentication when token is valid")
        void shouldSetAuthentication_whenTokenIsValid() throws ServletException, IOException {
            String token = "valid.jwt.token";
            String username = "test@example.com";

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.extractUsername(token)).thenReturn(username);
            when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
            when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);

            jwtAuthFilter.doFilterInternal(request, response, filterChain);

            assertInstanceOf(UsernamePasswordAuthenticationToken.class,
                    SecurityContextHolder.getContext().getAuthentication());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should skip authentication when token is missing and path is public")
        void shouldSkipAuth_whenTokenIsMissingAndPathIsPublic() throws ServletException, IOException {
            when(request.getHeader("Authorization")).thenReturn(null);
            when(request.getRequestURI()).thenReturn("/api/v1/users/login");

            jwtAuthFilter.doFilterInternal(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should not set authentication for invalid token")
        void shouldNotAuthenticate_whenTokenInvalid() throws ServletException, IOException {
            String token = "invalid.jwt.token";
            String username = "test@example.com";

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.extractUsername(token)).thenReturn(username);
            when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
            when(jwtService.isTokenValid(token, userDetails)).thenReturn(false); // token invalid

            jwtAuthFilter.doFilterInternal(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should handle exception when loading user fails")
        void shouldNotAuthenticate_whenUserDetailsFails() throws ServletException, IOException {
            String token = "valid.jwt.token";
            String username = "nonexistent@example.com";

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtService.extractUsername(token)).thenReturn(username);
            when(userDetailsService.loadUserByUsername(username)).thenThrow(new RuntimeException("User not found"));

            jwtAuthFilter.doFilterInternal(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("should return 401 and write JSON when JWT is malformed")
        void shouldReturn401_onJwtException() throws Exception {
            String token = "malformed.token";

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(request.getRequestURI()).thenReturn("/secure-endpoint");
            when(request.getMethod()).thenReturn("GET");
            when(request.getRemoteAddr()).thenReturn("127.0.0.1");

            PrintWriter writer = mock(PrintWriter.class);
            when(response.getWriter()).thenReturn(writer);

            doThrow(new io.jsonwebtoken.MalformedJwtException("Invalid token"))
                    .when(jwtService).extractUsername(token);

            jwtAuthFilter.doFilterInternal(request, response, filterChain);

            verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            verify(response).setContentType("application/json");
            verify(writer).write("{\"error\": \"Unauthorized: Invalid JWT\"}");
        }

    }
}
