package com.streaker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.PostgresTestContainerConfig;
import com.streaker.controller.log.dto.LogRequestDto;
import com.streaker.model.Category;
import com.streaker.model.Habit;
import com.streaker.model.Log;
import com.streaker.model.Streak;
import com.streaker.model.User;
import com.streaker.repository.CategoryRepository;
import com.streaker.repository.HabitRepository;
import com.streaker.repository.LogRepository;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WithMockUser(username = "testuser-log", roles = "USER")
@DisplayName("LogController Integration Tests")
class LogControllerIntegrationTest extends PostgresTestContainerConfig {

    public static final String TESTUSER_LOG = "testuser-log";
    public static final String EMAIL = "testuser-log@email.com";
    public static final String PASSWORD = "password1234";
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
    @Autowired
    private LogRepository logRepository;

    private UUID habitId;

    @BeforeEach
    void setup() {
        cleanDatabase();

        User user = userRepository.save(TestDataFactory.createUser(TESTUSER_LOG, EMAIL, PASSWORD));
        Category category = categoryRepository.save(TestDataFactory.createCategory(user));
        Streak streak = streakRepository.save(TestDataFactory.createStreak(user));
        Habit habit = habitRepository.save(TestDataFactory.createHabit(user, streak, category));

        habitId = habit.getUuid();
    }

    @Nested
    @DisplayName("POST /v1/users/habits/{habitId}/logs")
    class CreateLogTests {

        @Test
        @DisplayName("should persist and return log")
        void shouldCreateLog() throws Exception {
            LogRequestDto dto = new LogRequestDto(LocalDate.now(), true);

            mockMvc.perform(post("/v1/users/habits/{habitId}/logs", habitId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.completed").value(true))
                    .andExpect(jsonPath("$.habitId").value(habitId.toString()));

            List<Log> logs = logRepository.findByHabitUuid(habitId);
            assertThat(logs).hasSize(1);
            assertThat(logs.getFirst().getCompleted()).isTrue();
        }

        @Test
        @DisplayName("should return 400 if payload is invalid")
        void shouldReturn400ForInvalidPayload() throws Exception {
            // Missing required fields
            String invalidJson = "{}";

            mockMvc.perform(post("/v1/users/habits/{habitId}/logs", habitId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 404 if habit does not exist")
        void shouldReturn404IfHabitNotFound() throws Exception {
            UUID fakeHabitId = UUID.randomUUID();
            LogRequestDto dto = new LogRequestDto(LocalDate.now(), true);

            mockMvc.perform(post("/v1/users/habits/{habitId}/logs", fakeHabitId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /v1/users/habits/{habitId}/logs")
    class GetLogsTests {

        @Test
        @DisplayName("should return all logs for habit")
        void shouldReturnLogs() throws Exception {
            Habit habit = habitRepository.findById(habitId).orElseThrow();
            logRepository.save(TestDataFactory.createLog(habit, true));

            mockMvc.perform(get("/v1/users/habits/{habitId}/logs", habitId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].completed").value(true));
        }

        @Test
        @DisplayName("should return 404 if habit not found")
        void shouldReturn404IfHabitMissing() throws Exception {
            UUID fakeId = UUID.randomUUID();

            mockMvc.perform(get("/v1/users/habits/{habitId}/logs", fakeId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("POST /v1/users/habits/{habitId}/logs/{logId}/complete")
    class MarkCompletedTests {

        @Test
        @DisplayName("should update completed to true")
        void shouldMarkCompleted() throws Exception {
            Habit habit = habitRepository.findById(habitId).orElseThrow();
            Log log = logRepository.save(TestDataFactory.createLog(habit, false));

            mockMvc.perform(post("/v1/users/habits/{habitId}/logs/{logId}/complete", habitId, log.getUuid()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.completed").value(true));

            Log updated = logRepository.findById(log.getUuid()).orElseThrow();
            assertThat(updated.getCompleted()).isTrue();
        }

        @Test
        @DisplayName("should return 404 if log doesn't exist")
        void shouldReturn404ForInvalidLogId() throws Exception {
            UUID fakeLogId = UUID.randomUUID();

            mockMvc.perform(post("/v1/users/habits/{habitId}/logs/{logId}/complete", habitId, fakeLogId))
                    .andExpect(status().isNotFound());
        }
    }
}
