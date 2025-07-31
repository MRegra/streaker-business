package com.streaker.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {

    @Mock
    private JwtAuthFilter jwtAuthFilter;

    @Mock
    private Environment environment;

    @Mock
    private CustomUserDetailsService userDetailsService;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(environment, jwtAuthFilter, userDetailsService);
    }

    @Nested
    @DisplayName("passwordEncoder()")
    class PasswordEncoderTest {

        @Test
        @DisplayName("should return a working BCryptPasswordEncoder")
        void shouldCreatePasswordEncoder() {
            PasswordEncoder encoder = securityConfig.passwordEncoder();

            assertNotNull(encoder, "Password encoder should not be null");
            assertTrue(encoder.matches("test123", encoder.encode("test123")), "Encoded password should match raw password");
        }
    }
}
