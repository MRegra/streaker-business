package com.streaker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.PostgresTestContainerConfig;
import com.streaker.controller.habit.dto.HabitRequestDto;
import com.streaker.model.Category;
import com.streaker.model.Habit;
import com.streaker.model.Streak;
import com.streaker.model.User;
import com.streaker.repository.CategoryRepository;
import com.streaker.repository.HabitRepository;
import com.streaker.repository.StreakRepository;
import com.streaker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static com.streaker.utlis.enums.Frequency.DAILY;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WithMockUser(username = "testuser-habit", roles = "USER")
public class HabitControllerIntegrationTest extends PostgresTestContainerConfig {

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

    @BeforeEach
    void setup() {
        cleanDatabase();

        User user = new User();
        user.setUsername("testuser-habit");
        user.setEmail("testuser-habit@example.com");
        user.setPassword("password123");
        user = userRepository.save(user);
        userId = user.getUuid();

        Category category = new Category();
        category.setName("Fitness-Habit");
        category.setColor("#FF0000");
        category.setUser(user);
        category = categoryRepository.save(category);
        categoryId = category.getUuid();

        Streak streak = new Streak();
        streak.setName("Hydration-Habit");
        streak.setCurrentCount(0);
        streak.setIsActive(true);
        streak.setLongestStreak(0);
        streak.setDescription("Stay hydrated");
        streak.setStartDate(java.time.LocalDate.now());
        streak.setUser(user);
        streak = streakRepository.save(streak);
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
        Habit habit = new Habit();
        habit.setName("Run-Habit");
        habit.setDescription("Run daily");
        habit.setFrequency(DAILY);
        habit.setUser(userRepository.findById(userId).orElseThrow());
        habit.setCategory(categoryRepository.findById(categoryId).orElseThrow());
        habit.setStreak(streakRepository.findById(streakId).orElseThrow());
        habitRepository.save(habit);

        mockMvc.perform(get("/v1/users/{userId}/habits", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Run-Habit"));
    }

    @Test
    void getHabitById_shouldReturnHabit() throws Exception {
        Habit habit = new Habit();
        habit.setName("Workout-Habit");
        habit.setDescription("Daily exercise");
        habit.setFrequency(DAILY);
        habit.setUser(userRepository.findById(userId).orElseThrow());
        habit.setCategory(categoryRepository.findById(categoryId).orElseThrow());
        habit.setStreak(streakRepository.findById(streakId).orElseThrow());
        habit = habitRepository.save(habit);

        mockMvc.perform(get("/v1/users/{userId}/habits/{habitId}", userId, habit.getUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(habit.getUuid().toString()));
    }

    @Test
    void deleteHabit_shouldRemoveHabit() throws Exception {
        Habit habit = new Habit();
        habit.setName("DeleteMe-Habit");
        habit.setDescription("To be deleted");
        habit.setFrequency(DAILY);
        habit.setUser(userRepository.findById(userId).orElseThrow());
        habit.setCategory(categoryRepository.findById(categoryId).orElseThrow());
        habit.setStreak(streakRepository.findById(streakId).orElseThrow());
        habit = habitRepository.save(habit);

        mockMvc.perform(delete("/v1/users/{userId}/habits/{habitId}", userId, habit.getUuid()))
                .andExpect(status().isNoContent());

        assertThat(habitRepository.findById(habit.getUuid())).isEmpty();
    }
}