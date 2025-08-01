package com.streaker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.BaseIntegrationTest;
import com.streaker.TestContainerConfig;
import com.streaker.controller.auth.dto.AuthTokensResponse;
import com.streaker.integration.utils.IntegrationTestUtils;
import com.streaker.model.User;
import com.streaker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestContainerConfig.class)
public class UserControllerIntegrationTest extends BaseIntegrationTest {

    private static final String USERNAME = "john";
    private static final String EMAIL = "john@example.com";
    private static final String PASSWORD = "securePassword1";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    private String jwt;
    private UUID userId;

    @BeforeEach
    void setup() throws Exception {
        cleanDatabase();
        AuthTokensResponse tokens = IntegrationTestUtils.registerAndLogin(mockMvc, objectMapper, USERNAME, EMAIL, PASSWORD);
        this.jwt = tokens.accessToken();
        this.userId = userRepository.findByEmail(EMAIL).orElseThrow().getUuid();
    }

    @Test
    @DisplayName("Should persist user correctly on registration")
    void createUser_shouldPersistAndReturnUser() {
        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(1);
        assertThat(users.getFirst().getEmail()).isEqualTo(EMAIL);
    }

    @Nested
    @DisplayName("GET /v1/users/{id}")
    class GetUserById {

        @Test
        @DisplayName("Should return user with valid JWT")
        void shouldReturnUser_withValidJwt() throws Exception {
            mockMvc.perform(get("/v1/users/{id}", userId)
                            .header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(USERNAME))
                    .andExpect(jsonPath("$.email").value(EMAIL));
        }

        @Test
        @DisplayName("Should return 403 for valid JWT but wrong user ID")
        void shouldReturn403_forUnauthorizedUser() throws Exception {
            UUID randomId = UUID.randomUUID();

            mockMvc.perform(get("/v1/users/{id}", randomId)
                            .header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 for invalid JWT")
        void shouldReturn401_forInvalidToken() throws Exception {
            mockMvc.perform(get("/v1/users/{id}", userId)
                            .header("Authorization", "Bearer invalid.token.here"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 when JWT is missing")
        void shouldReturn400_withoutJwt() throws Exception {
            mockMvc.perform(get("/v1/users/{id}", userId))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 403 for another registered user")
        void shouldReturn403_forAnotherRegisteredUser() throws Exception {
            AuthTokensResponse tokens = IntegrationTestUtils.registerAndLogin(
                    mockMvc, objectMapper, "intruder", "intruder@example.com", "hackMe123");

            mockMvc.perform(get("/v1/users/{id}", userId)
                            .header("Authorization", "Bearer " + tokens.accessToken()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /v1/users/register validation")
    class RegisterValidation {

        private static final String REGISTER_URL = "/v1/users/register";

        @Test
        void shouldFailWhenAllFieldsAreBlank() throws Exception {
            String payload = """
                        {
                          "username": "",
                          "email": "",
                          "password": ""
                        }
                    """;

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("Username is required")))
                    .andExpect(jsonPath("$.message", containsString("Email is required")))
                    .andExpect(jsonPath("$.message", containsString("password: Password must contain at least one lowercase letter")));
        }

        @Test
        void shouldFailWhenEmailIsInvalid() throws Exception {
            String payload = """
                        {
                          "username": "testuser",
                          "email": "invalid-email",
                          "password": "validPass123"
                        }
                    """;

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("email: Invalid email format")));
        }

        @Test
        void shouldFailWhenPasswordTooShort() throws Exception {
            String payload = """
                        {
                          "username": "user",
                          "email": "user@example.com",
                          "password": "123"
                        }
                    """;

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("password: Password must be between 8 and 64")));
        }

        @Test
        void shouldSucceedWithValidInput() throws Exception {
            String payload = """
                        {
                          "username": "validuser",
                          "email": "validuser@example.com",
                          "password": "strongPass123"
                        }
                    """;

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isOk());
        }
    }
}
