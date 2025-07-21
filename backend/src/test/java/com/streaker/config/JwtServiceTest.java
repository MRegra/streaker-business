package com.streaker.config;

import com.streaker.model.User;
import com.streaker.utlis.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret=your-test-secret-key-your-test-secret-key",
        "jwt.expiration-ms=3600000"
})
public class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldGenerateAndValidateToken() {
        User user = new User();
        user.setUsername("test@example.com");
        user.setRole(Role.USER);
        String token = jwtService.generateToken(user);

        assertNotNull(token);
        String role = jwtService.extractRole(token);
        assertEquals("USER", role);
    }

    @Test
    void shouldExtractEmailFromToken() {
        User user = new User();
        user.setUsername("test@example.com");
        user.setRole(Role.USER);
        String token = jwtService.generateToken(user);

        String extracted = jwtService.extractUsername(token);
        assertEquals("test@example.com", extracted);
    }

    @Test
    void shouldExtractRoleFromToken() {
        User user = new User();
        user.setUsername("test@example.com");
        user.setRole(Role.ADMIN);

        String token = jwtService.generateToken(user);
        String role = jwtService.extractRole(token);

        assertEquals("ADMIN", role);
    }

}
