package com.streaker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.BaseIntegrationTest;
import com.streaker.TestContainerConfig;
import com.streaker.controller.auth.dto.AuthTokensResponse;
import com.streaker.integration.utils.IntegrationTestUtils;
import com.streaker.model.User;
import com.streaker.repository.CategoryRepository;
import com.streaker.repository.HabitRepository;
import com.streaker.repository.StreakRepository;
import com.streaker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestContainerConfig.class)
public class UserControllerIntegrationTest extends BaseIntegrationTest {

    private static final String USERNAME = "john";
    private static final String EMAIL = "john@example.com";
    private static final String PASSWORD = "securePassword";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private HabitRepository habitRepository;
    @Autowired
    private StreakRepository streakRepository;

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
            String intruderEmail = "intruder@example.com";
            String intruderUsername = "intruder";
            String intruderPassword = "hackMe123";

            AuthTokensResponse intruderTokens = IntegrationTestUtils.registerAndLogin(
                    mockMvc, objectMapper, intruderUsername, intruderEmail, intruderPassword);

            mockMvc.perform(get("/v1/users/{id}", userId)
                            .header("Authorization", "Bearer " + intruderTokens.accessToken()))
                    .andExpect(status().isForbidden());
        }
    }
}
