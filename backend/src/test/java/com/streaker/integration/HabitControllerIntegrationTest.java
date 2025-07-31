package com.streaker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.PostgresTestContainerConfig;
import com.streaker.controller.auth.dto.AuthTokensResponse;
import com.streaker.controller.habit.dto.HabitRequestDto;
import com.streaker.integration.utils.IntegrationTestUtils;
import com.streaker.repository.CategoryRepository;
import com.streaker.repository.HabitRepository;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.streaker.utlis.enums.Frequency.DAILY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DisplayName("HabitController Integration Tests")
class HabitControllerIntegrationTest extends PostgresTestContainerConfig {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private StreakRepository streakRepository;
    @Autowired
    private HabitRepository habitRepository;

    private UUID userId;
    private UUID categoryId;
    private UUID streakId;
    private String jwt;

    @BeforeEach
    void setup() throws Exception {
        cleanDatabase();

        AuthTokensResponse tokens = IntegrationTestUtils.registerAndLogin(mockMvc, objectMapper, "habit-user", "habit@example.com", "strongpass");
        jwt = tokens.accessToken();
        userId = userRepository.findByEmail("habit@example.com").orElseThrow().getUuid();

        var user = userRepository.findById(userId).orElseThrow();
        categoryId = categoryRepository.save(TestDataFactory.createCategory(user)).getUuid();
        streakId = streakRepository.save(TestDataFactory.createStreak(user)).getUuid();
    }

    @Nested
    @DisplayName("POST /v1/users/{userId}/habits")
    class CreateHabit {

        @Test
        @DisplayName("Should create habit successfully with valid request")
        void shouldCreateHabitSuccessfully() throws Exception {
            HabitRequestDto dto = new HabitRequestDto("Hydration-Habit", "Drink water", DAILY, categoryId, streakId);

            mockMvc.perform(post("/v1/users/{userId}/habits", userId)
                            .header("Authorization", "Bearer " + jwt)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Hydration-Habit"));

            assertThat(habitRepository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("Should return 401 if missing Authorization header")
        void shouldReturn401WithoutAuth() throws Exception {
            HabitRequestDto dto = new HabitRequestDto("Hydration-Habit", "Drink water", DAILY, categoryId, streakId);

            mockMvc.perform(post("/v1/users/{userId}/habits", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for malformed JSON")
        void shouldReturn400ForInvalidPayload() throws Exception {
            String invalidJson = "{\"name\": \"OnlyName\"}";

            mockMvc.perform(post("/v1/users/{userId}/habits", userId)
                            .header("Authorization", "Bearer " + jwt)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /v1/users/{userId}/habits")
    class GetAllHabits {

        @Test
        @DisplayName("Should return all habits for authenticated user")
        void shouldReturnAllHabits() throws Exception {
            var user = userRepository.findById(userId).orElseThrow();
            var category = categoryRepository.findById(categoryId).orElseThrow();
            var streak = streakRepository.findById(streakId).orElseThrow();

            habitRepository.save(TestDataFactory.createHabit(user, streak, category));

            mockMvc.perform(get("/v1/users/{userId}/habits", userId)
                            .header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }
    }

    @Nested
    @DisplayName("GET /v1/users/{userId}/habits/{habitId}")
    class GetHabitById {

        @Test
        @DisplayName("Should return habit for valid ID")
        void shouldReturnHabitById() throws Exception {
            var user = userRepository.findById(userId).orElseThrow();
            var category = categoryRepository.findById(categoryId).orElseThrow();
            var streak = streakRepository.findById(streakId).orElseThrow();

            var habit = habitRepository.save(TestDataFactory.createHabit(user, streak, category));

            mockMvc.perform(get("/v1/users/{userId}/habits/{habitId}", userId, habit.getUuid())
                            .header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.uuid").value(habit.getUuid().toString()));
        }

        @Test
        @DisplayName("Should return 404 if habit does not exist")
        void shouldReturn404IfHabitNotFound() throws Exception {
            mockMvc.perform(get("/v1/users/{userId}/habits/{habitId}", userId, UUID.randomUUID())
                            .header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /v1/users/{userId}/habits/{habitId")
    class DeleteHabit {

        @Test
        @DisplayName("Should delete habit by ID")
        void shouldDeleteHabit() throws Exception {
            var user = userRepository.findById(userId).orElseThrow();
            var category = categoryRepository.findById(categoryId).orElseThrow();
            var streak = streakRepository.findById(streakId).orElseThrow();

            var habit = habitRepository.save(TestDataFactory.createHabit(user, streak, category));

            mockMvc.perform(delete("/v1/users/{userId}/habits/{habitId}", userId, habit.getUuid())
                            .header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isNoContent());

            assertThat(habitRepository.findById(habit.getUuid())).isEmpty();
        }

        @Test
        @DisplayName("Should return 404 if trying to delete non-existent habit")
        void shouldReturn404IfDeletingNonexistentHabit() throws Exception {
            mockMvc.perform(delete("/v1/users/{userId}/habits/{habitId}", userId, UUID.randomUUID())
                            .header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isNotFound());
        }
    }
}