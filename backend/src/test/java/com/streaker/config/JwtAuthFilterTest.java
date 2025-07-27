package com.streaker.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JwtAuthFilterTest {

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
    }

    @Test
    void shouldSetAuthentication_whenTokenIsValid() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String username = "test@example.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertInstanceOf(UsernamePasswordAuthenticationToken.class, SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldSkipAuth_whenTokenIsInvalid() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/v1/users/login");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
