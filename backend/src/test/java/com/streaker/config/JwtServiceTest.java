package com.streaker.config;

import com.streaker.PostgresTestContainerConfig;
import com.streaker.controller.user.dto.UserResponseDto;
import com.streaker.utlis.enums.Role;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("JwtService Tests")
class JwtServiceTest extends PostgresTestContainerConfig {

    @Autowired
    private JwtService jwtService;

    private UserDetails userDetails;
    private UserDetails adminUserDetails;
    private UserResponseDto userResponseDto;
    private UserResponseDto adminUserResponseDto;

    @BeforeEach
    void setup() {
        userResponseDto = new UserResponseDto(
                UUID.randomUUID(),
                "test@example.com",
                "test@example.com",
                Role.USER
        );

        adminUserResponseDto = new UserResponseDto(
                UUID.randomUUID(),
                "test1@example.com",
                "test1@example.com",
                Role.ADMIN
        );

        userDetails = new User(
                "test@example.com",
                "password123",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        adminUserDetails = new User(
                "test1@example.com",
                "password123",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    @Nested
    @DisplayName("generateToken + extractRole/Username")
    class TokenGenerationAndExtraction {

        @Test
        @DisplayName("should generate token and extract role correctly for USER")
        void shouldGenerateAndValidateToken() throws Exception {
            String token = jwtService.generateToken(userDetails, userResponseDto);

            assertNotNull(token);
            String role = jwtService.extractRole(token);
            assertEquals("ROLE_USER", role);
        }

        @Test
        @DisplayName("should extract username (email) from token")
        void shouldExtractEmailFromToken() throws MalformedJwtException {
            String token = jwtService.generateToken(userDetails, userResponseDto);

            String extracted = jwtService.extractUsername(token);
            assertEquals("test@example.com", extracted);
        }

        @Test
        @DisplayName("should extract role from ADMIN token")
        void shouldExtractRoleFromToken() throws Exception {
            String token = jwtService.generateToken(adminUserDetails, adminUserResponseDto);

            String role = jwtService.extractRole(token);
            assertEquals("ROLE_ADMIN", role);
        }
    }
}
