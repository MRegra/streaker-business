package com.streaker.config;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${environment:}")
    private String environment;

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/actuator/health",
            "/api/v1/users/login",
            "/api/v1/users/register"
    );

    public JwtAuthFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        String remoteIp = request.getRemoteAddr();

        try {
            log.debug("Incoming request: [{}] {} from IP: {}", method, path, remoteIp);

            final String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                if (!PUBLIC_PATHS.contains(path) && !isDev()) {
                    log.warn("No JWT token provided in Authorization header for request to {}", path);
                }
                filterChain.doFilter(request, response);
                return;
            }

            final String jwt = authHeader.substring(7);
            final String username = jwtService.extractUsername(jwt);

            log.debug("Extracted JWT for user: {}", username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails;
                try {
                    userDetails = userDetailsService.loadUserByUsername(username);
                } catch (Exception e) {
                    log.error("Failed to load user details for username: {}", username, e);
                    filterChain.doFilter(request, response);
                    return;
                }

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("Authentication set for user: {}", username);
                } else {
                    log.error("Invalid JWT token for user: {}", username);
                }
            }

            filterChain.doFilter(request, response);

        } catch (JwtException ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            log.error("JWT error during [{}] {} from IP {}: {}", method, path, remoteIp, ex.getMessage(), ex);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Unauthorized: Invalid JWT\"}");
        }
    }

    private boolean isDev() {
        return environment.equalsIgnoreCase("DEV");
    }
}
