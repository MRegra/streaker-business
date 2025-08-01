package com.streaker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.BaseIntegrationTest;
import com.streaker.TestContainerConfig;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DisplayName("AuthController Integration Tests")
@Import(TestContainerConfig.class)
public class AuthControllerIntegrationTest extends BaseIntegrationTest {

    public static final String USER = "refreshUser";
    public static final String EMAIL = "refresh@example.com";
    public static final String PASSWORD = "Refresh123";
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
                mockMvc, objectMapper, USER, EMAIL, PASSWORD
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
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.message", containsString("refreshToken: Refresh token must not be blank")));
        }

        @Test
        @DisplayName("should return 401 Unauthorized for expired refresh token")
        void expiredRefreshToken_returnsUnauthorized() throws Exception {
            // Generate a short-lived token for test purposes
            String expiredToken = IntegrationTestUtils.generateExpiredRefreshToken(USER, jwtSecret);

            RefreshTokenRequest request = new RefreshTokenRequest(expiredToken);

            mockMvc.perform(post("/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Refresh token expired"));
        }
    }
}
