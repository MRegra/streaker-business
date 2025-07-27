package com.streaker.config;

import com.streaker.exception.UnauthorizedAccessException;
import com.streaker.utlis.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.crypto.SecretKey;

@Component
public class JwtAuthorizationValidator {

    @Value("${jwt.secret:}")
    private String jwtSecret;

    public void validateToken(String token) {
        SecretKey secretKey;
        if (jwtSecret != null) {
            secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        }
        else {
            throw new JwtException("Invalid JWT Secret");
        }
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .getBody();

            String role = claims.get("role", String.class);

            if (!role.equals(Role.ADMIN.name())) {
                throw new UnauthorizedAccessException("User does not have permission to access this resource.");
            }

        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedAccessException("Invalid JWT token.");
        }
    }

    public void validateToken(String token, UUID pathUserId) {
        SecretKey secretKey;
        if (jwtSecret != null) {
            secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        }
        else {
            throw new JwtException("Invalid JWT Secret");
        }
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .getBody();

            String uuidFromToken = claims.get("uuid", String.class);
            UUID tokenUserId = UUID.fromString(uuidFromToken);

            if (!tokenUserId.equals(pathUserId)) {
                throw new UnauthorizedAccessException("User does not have permission to access this resource.");
            }

        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedAccessException("Invalid JWT token.");
        }
    }
}