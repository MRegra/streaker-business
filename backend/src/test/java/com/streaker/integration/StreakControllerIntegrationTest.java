package com.streaker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.BaseIntegrationTest;
import com.streaker.TestContainerConfig;
import com.streaker.controller.auth.dto.AuthTokensResponse;
import com.streaker.integration.utils.IntegrationTestUtils;
import com.streaker.model.Streak;
import com.streaker.model.User;
import com.streaker.repository.StreakRepository;
import com.streaker.repository.UserRepository;
import com.streaker.utils.TestDataFactory;
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

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DisplayName("StreakController Integration Tests")
@Import(TestContainerConfig.class)
class StreakControllerIntegrationTest extends BaseIntegrationTest {

    private static final String TESTUSER_STREAK = "testuser_streak";
    private static final String EMAIL = "testuser-streak@example.com";
    private static final String PASSWORD = "Password123";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StreakRepository streakRepository;

    private UUID userId;
    private String jwt;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();

        AuthTokensResponse tokens = IntegrationTestUtils.registerAndLogin(
                mockMvc, objectMapper,
                TESTUSER_STREAK, EMAIL, PASSWORD
        );

        jwt = tokens.accessToken();
        userId = userRepository.findByEmail(EMAIL).orElseThrow().getUuid();
    }

    @Nested
    @DisplayName("GET /v1/users/{userId}/streaks")
    class GetAllStreaks {

        @Test
        @DisplayName("Should return all streaks for a user")
        void shouldReturnAllStreaks() throws Exception {
            User user = userRepository.findById(userId).orElseThrow();
            streakRepository.save(TestDataFactory.createStreak(user));
            streakRepository.save(TestDataFactory.createStreak(user));

            mockMvc.perform(get("/v1/users/{userId}/streaks", userId)
                            .header("Authorization", "Bearer " + jwt)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("Should return empty list if user has no streaks")
        void shouldReturnEmptyList() throws Exception {
            mockMvc.perform(get("/v1/users/{userId}/streaks", userId)
                            .header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Should return 403 if user does not exist - Forbidden")
        void shouldReturnNotFoundForInvalidUser() throws Exception {
            UUID fakeUserId = UUID.randomUUID();

            mockMvc.perform(get("/v1/users/{userId}/streaks", fakeUserId)
                            .header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /v1/users/{userId}/streaks/{streakId}")
    class GetStreakById {

        @Test
        @DisplayName("Should return the correct streak for user")
        void shouldReturnCorrectStreak() throws Exception {
            User user = userRepository.findById(userId).orElseThrow();
            Streak streak = streakRepository.save(TestDataFactory.createStreak(user));

            mockMvc.perform(get("/v1/users/{userId}/streaks/{streakId}", userId, streak.getUuid())
                            .header("Authorization", "Bearer " + jwt)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.uuid").value(streak.getUuid().toString()));
        }

        @Test
        @DisplayName("Should return 404 if streak not found")
        void shouldReturnNotFound() throws Exception {
            UUID fakeStreakId = UUID.randomUUID();

            mockMvc.perform(get("/v1/users/{userId}/streaks/{streakId}", userId, fakeStreakId)
                            .header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isNotFound());
        }
    }
}
