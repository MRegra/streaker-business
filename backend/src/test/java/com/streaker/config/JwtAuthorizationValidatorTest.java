package com.streaker.config;

import com.streaker.exception.UnauthorizedAccessException;
import com.streaker.utlis.enums.Role;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class JwtAuthorizationValidatorTest {

    @Autowired
    private JwtAuthorizationValidator validator;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private String generateToken(String role, UUID uuid) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject("test-user")
                .claim("role", role)
                .claim("uuid", uuid.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60)) // 1 min
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void validateToken_shouldPassForAdmin() {
        String token = "Bearer " + generateToken(Role.ADMIN.name(), UUID.randomUUID());
        assertDoesNotThrow(() -> validator.validateToken(token));
    }

    @Test
    void validateToken_shouldThrowForNonAdmin() {
        String token = "Bearer " + generateToken(Role.USER.name(), UUID.randomUUID());
        UnauthorizedAccessException ex = assertThrows(
                UnauthorizedAccessException.class,
                () -> validator.validateToken(token)
        );
        assertEquals("User does not have permission to access this resource.", ex.getMessage());
    }

    @Test
    void validateToken_shouldThrowForMalformedToken() {
        String malformedToken = "Bearer this.is.not.valid.jwt";
        assertThrows(UnauthorizedAccessException.class, () -> validator.validateToken(malformedToken));
    }

    @Test
    void validateTokenWithUUID_shouldPassIfUUIDMatches() {
        UUID uuid = UUID.randomUUID();
        String token = "Bearer " + generateToken(Role.USER.name(), uuid);
        assertDoesNotThrow(() -> validator.validateToken(token, uuid));
    }

    @Test
    void validateTokenWithUUID_shouldThrowIfUUIDMismatch() {
        UUID uuidFromToken = UUID.randomUUID();
        UUID mismatchedUUID = UUID.randomUUID();
        String token = "Bearer " + generateToken(Role.USER.name(), uuidFromToken);

        UnauthorizedAccessException ex = assertThrows(
                UnauthorizedAccessException.class,
                () -> validator.validateToken(token, mismatchedUUID)
        );
        assertEquals("User does not have permission to access this resource.", ex.getMessage());
    }

    @Test
    void validateToken_shouldThrowIfNoSecretSet() {
        JwtAuthorizationValidator brokenValidator = new JwtAuthorizationValidator();
        ReflectionTestUtils.setField(brokenValidator, "jwtSecret", null);
        assertThrows(JwtException.class, () -> brokenValidator.validateToken("Bearer dummy"));
    }
}
