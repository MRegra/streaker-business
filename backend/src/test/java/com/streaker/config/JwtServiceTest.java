package com.streaker.config;

import com.streaker.PostgresTestContainerConfig;
import org.springframework.security.core.userdetails.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret=your-test-secret-key-your-test-secret-key",
        "jwt.expiration-ms=3600000"
})
public class JwtServiceTest extends PostgresTestContainerConfig {

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldGenerateAndValidateToken() {
        // Create a UserDetails object with a role
        UserDetails userDetails = new User(
                "test@example.com",
                "password123",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);

        String role = jwtService.extractRole(token);
        assertEquals("ROLE_USER", role);
    }

    @Test
    void shouldExtractEmailFromToken() {
        UserDetails userDetails = new User(
                "test@example.com",
                "password123",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        String token = jwtService.generateToken(userDetails);

        String extracted = jwtService.extractUsername(token);
        assertEquals("test@example.com", extracted);
    }

    @Test
    void shouldExtractRoleFromToken() {
        UserDetails userDetails = new User(
                "test@example.com",
                "password123",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        String token = jwtService.generateToken(userDetails);
        String role = jwtService.extractRole(token);

        assertEquals("ROLE_ADMIN", role);
    }

}
