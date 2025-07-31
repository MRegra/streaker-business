package com.streaker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.PostgresTestContainerConfig;
import com.streaker.controller.auth.dto.AuthTokensResponse;
import com.streaker.controller.auth.dto.RefreshTokenRequest;
import com.streaker.integration.utils.IntegrationTestUtils;
import com.streaker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthControllerIntegrationTest extends PostgresTestContainerConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    private String refreshToken;

    @BeforeEach
    void setup() throws Exception {
        cleanDatabase();

        AuthTokensResponse tokens = IntegrationTestUtils.registerLoginAndGetTokens(
                mockMvc, objectMapper, "refreshUser", "refresh@example.com", "refresh123"
        );
        this.refreshToken = tokens.refreshToken();
    }

    @Nested
    @DisplayName("POST /v1/auth/refresh")
    class RefreshToken {

        @Test
        @DisplayName("should return new access and refresh tokens for valid refresh token")
        void validRefreshToken_returnsNewTokens() throws Exception {
            RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);

            mockMvc.perform(post("/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.refreshToken").exists());
        }

        @Test
        @DisplayName("should return 401 Unauthorized for invalid refresh token")
        void invalidRefreshToken_returnsUnauthorized() throws Exception {
            RefreshTokenRequest request = new RefreshTokenRequest("bad.token.value");

            mockMvc.perform(post("/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Invalid token"));
        }

        @Test
        @DisplayName("should return 400 Bad Request when token is missing")
        void missingRefreshToken_returnsBadRequest() throws Exception {
            RefreshTokenRequest request = new RefreshTokenRequest(null);

            mockMvc.perform(post("/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Missing refresh token"));
        }

        @Test
        @DisplayName("should return 401 Unauthorized for expired refresh token")
        void expiredRefreshToken_returnsUnauthorized() throws Exception {
            // Generate a short-lived token for test purposes
            String expiredToken = IntegrationTestUtils.generateExpiredRefreshToken("refreshUser", jwtSecret);

            RefreshTokenRequest request = new RefreshTokenRequest(expiredToken);

            mockMvc.perform(post("/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Refresh token expired"));
        }
    }
}
