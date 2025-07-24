package com.streaker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.PostgresTestContainerConfig;
import com.streaker.controller.log.dto.LogRequestDto;
import com.streaker.model.Category;
import com.streaker.model.Habit;
import com.streaker.model.Log;
import com.streaker.model.Streak;
import com.streaker.model.User;
import com.streaker.repository.HabitRepository;
import com.streaker.repository.LogRepository;
import com.streaker.repository.UserRepository;
import com.streaker.utlis.enums.Frequency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser-log", roles = "USER")
class LogControllerIntegrationTest extends PostgresTestContainerConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private LogRepository logRepository;

    private UUID habitId;

    @BeforeEach
    void setup() {
        cleanDatabase();

        User user = new User();
        user.setUsername("testuser-log");
        user.setEmail("testuser-log@example.com");
        user.setPassword("password123");
        user = userRepository.save(user);

        Category category = new Category();
        category.setName("Wellness");
        category.setUser(user);
        category.setColor("#FFFFFF");
        category = categoryRepository.save(category);

        Streak streak = new Streak();
        streak.setName("reading-streak");
        streak.setIsActive(true);
        streak.setCurrentCount(0);
        streak.setStartDate(LocalDate.now());
        streak.setUser(user);
        streak = streakRepository.save(streak);

        Habit habit = new Habit();
        habit.setName("Read");
        habit.setUser(user);
        habit.setStreak(streak);
        habit.setFrequency(Frequency.DAILY);
        habit.setDescription("Reading at least 10 pages");
        habit.setCategory(category);
        habit = habitRepository.save(habit);

        habitId = habit.getUuid();
    }

    @Test
    void createLog_shouldPersistAndReturnLog() throws Exception {
        LogRequestDto dto = new LogRequestDto(LocalDate.now(), true);

        mockMvc.perform(post("/users/habits/{habitId}/logs", habitId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.habitId").value(habitId.toString()));

        List<Log> saved = logRepository.findByHabitUuid(habitId);
        assertThat(saved).hasSize(1);
        assertThat(saved.getFirst().getCompleted()).isTrue();
    }

    @Test
    void getLogs_shouldReturnLogsForHabit() throws Exception {
        Log log = new Log();
        log.setDate(LocalDate.now());
        log.setCompleted(true);
        log.setHabit(habitRepository.findById(habitId).orElseThrow());
        logRepository.save(log);

        mockMvc.perform(get("/users/habits/{habitId}/logs", habitId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].completed").value(true));
    }

    @Test
    void getLogById_shouldReturnSingleLog() throws Exception {
        Log log = new Log();
        log.setDate(LocalDate.now());
        log.setCompleted(false);
        log.setHabit(habitRepository.findById(habitId).orElseThrow());
        log = logRepository.save(log);

        mockMvc.perform(get("/users/habits/{habitId}/logs/{logId}", habitId, log.getUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(log.getUuid().toString()))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void markLogCompleted_shouldSetCompletedToTrue() throws Exception {
        Log log = new Log();
        log.setDate(LocalDate.now());
        log.setCompleted(false);
        log.setHabit(habitRepository.findById(habitId).orElseThrow());
        log = logRepository.save(log);

        mockMvc.perform(post("/users/habits/{habitId}/logs/{logId}/complete", habitId, log.getUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));

        Log updated = logRepository.findById(log.getUuid()).orElseThrow();
        assertThat(updated.getCompleted()).isTrue();
    }
}
