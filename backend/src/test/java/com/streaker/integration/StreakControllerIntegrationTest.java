package com.streaker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.BaseIntegrationTest;
import com.streaker.TestContainerConfig;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WithMockUser(username = "testuser-streak", roles = "USER")
@DisplayName("StreakController Integration Tests")
@Import(TestContainerConfig.class)
class StreakControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StreakRepository streakRepository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        User user = TestDataFactory.createUser("testuser-streak", "testuser-streak@example.com", "password123");
        userId = userRepository.save(user).getUuid();
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
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("Should return empty list if user has no streaks")
        void shouldReturnEmptyList() throws Exception {
            mockMvc.perform(get("/v1/users/{userId}/streaks", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Should return empty list if user does not exist")
        void shouldReturnEmptyForInvalidUser() throws Exception {
            UUID fakeUserId = UUID.randomUUID();
            mockMvc.perform(get("/v1/users/{userId}/streaks", fakeUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
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
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.uuid").value(streak.getUuid().toString()));
        }

        @Test
        @DisplayName("Should return 404 if streak not found")
        void shouldReturnNotFound() throws Exception {
            UUID fakeStreakId = UUID.randomUUID();
            mockMvc.perform(get("/v1/users/{userId}/streaks/{streakId}", userId, fakeStreakId))
                    .andExpect(status().isNotFound());
        }
    }
}
