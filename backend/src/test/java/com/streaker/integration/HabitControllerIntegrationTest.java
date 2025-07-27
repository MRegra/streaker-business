package com.streaker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.PostgresTestContainerConfig;
import com.streaker.controller.habit.dto.HabitRequestDto;
import com.streaker.integration.utils.IntegrationTestUtils;
import com.streaker.model.Category;
import com.streaker.model.Habit;
import com.streaker.model.Streak;
import com.streaker.repository.CategoryRepository;
import com.streaker.repository.HabitRepository;
import com.streaker.repository.StreakRepository;
import com.streaker.repository.UserRepository;
import com.streaker.utils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static com.streaker.utlis.enums.Frequency.DAILY;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class HabitControllerIntegrationTest extends PostgresTestContainerConfig {

    private static final String HABIT_USER_TEST = "habit-user-test";
    private static final String EMAIL = "habit@example.com";
    private static final String PASSWORD = "strongpass";

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
        this.jwt = IntegrationTestUtils.registerAndLogin(mockMvc, objectMapper, HABIT_USER_TEST, EMAIL, PASSWORD);
        this.userId = userRepository.findByEmail(EMAIL).orElseThrow().getUuid();

        Category category = categoryRepository.save(TestDataFactory.createCategory(userRepository.findById(userId).orElseThrow()));
        categoryId = category.getUuid();

        Streak streak = streakRepository.save(TestDataFactory.createStreak(userRepository.findById(userId).orElseThrow()));
        streakId = streak.getUuid();
    }

    @Test
    void createHabit_shouldPersistAndReturnHabit() throws Exception {
        HabitRequestDto dto = new HabitRequestDto(
                "Hydration-Habit",
                "Drink water",
                DAILY,
                categoryId,
                streakId
        );

        mockMvc.perform(post("/v1/users/{userId}/habits", userId)
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Hydration-Habit"))
                .andExpect(jsonPath("$.categoryId").value(categoryId.toString()))
                .andExpect(jsonPath("$.streakId").value(streakId.toString()));

        List<Habit> saved = habitRepository.findAll();
        assertThat(saved).hasSize(1);
        assertThat(saved.getFirst().getName()).isEqualTo("Hydration-Habit");
    }

    @Test
    void getAllHabits_shouldReturnList() throws Exception {
        Habit habit = TestDataFactory.createHabit(
                userRepository.findById(userId).orElseThrow(),
                streakRepository.findById(streakId).orElseThrow(),
                categoryRepository.findById(categoryId).orElseThrow()
        );
        habit.setName("Run-Habit");
        habit.setDescription("Run daily");
        habitRepository.save(habit);

        mockMvc.perform(get("/v1/users/{userId}/habits", userId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Run-Habit"));
    }

    @Test
    void getHabitById_shouldReturnHabit() throws Exception {
        Habit habit = TestDataFactory.createHabit(
                userRepository.findById(userId).orElseThrow(),
                streakRepository.findById(streakId).orElseThrow(),
                categoryRepository.findById(categoryId).orElseThrow()
        );
        habit = habitRepository.save(habit);

        mockMvc.perform(get("/v1/users/{userId}/habits/{habitId}", userId, habit.getUuid())
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(habit.getUuid().toString()));
    }

    @Test
    void deleteHabit_shouldRemoveHabit() throws Exception {
        Habit habit = TestDataFactory.createHabit(
                userRepository.findById(userId).orElseThrow(),
                streakRepository.findById(streakId).orElseThrow(),
                categoryRepository.findById(categoryId).orElseThrow()
        );
        habit = habitRepository.save(habit);

        mockMvc.perform(delete("/v1/users/{userId}/habits/{habitId}", userId, habit.getUuid())
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isNoContent());

        assertThat(habitRepository.findById(habit.getUuid())).isEmpty();
    }
}