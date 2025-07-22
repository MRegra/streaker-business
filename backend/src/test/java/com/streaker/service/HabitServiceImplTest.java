package com.streaker.service;

import com.streaker.controller.habit.dto.HabitRequestDto;
import com.streaker.controller.habit.dto.HabitResponseDto;
import com.streaker.exception.ResourceNotFoundException;
import com.streaker.model.Category;
import com.streaker.model.Habit;
import com.streaker.model.Streak;
import com.streaker.model.User;
import com.streaker.repository.CategoryRepository;
import com.streaker.repository.HabitRepository;
import com.streaker.repository.StreakRepository;
import com.streaker.repository.UserRepository;
import com.streaker.utlis.enums.Frequency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
public class HabitServiceImplTest {

    @Mock
    private HabitRepository habitRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private StreakRepository streakRepository;

    @InjectMocks
    private HabitServiceImpl habitService;

    private UUID userId, habitId, categoryId, streakId;
    private User user;
    private Category category;
    private Streak streak;
    private HabitRequestDto requestDto;
    private Habit habit;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        streakId = UUID.randomUUID();
        habitId = UUID.randomUUID();

        user = new User(); user.setUuid(userId);
        category = new Category(); category.setUuid(categoryId);
        streak = new Streak(); streak.setUuid(streakId);

        requestDto = new HabitRequestDto("Workout", "Daily exercise", Frequency.DAILY, categoryId, streakId);
        habit = new Habit();
        habit.setUuid(habitId);
        habit.setName("Workout");
        habit.setDescription("Daily exercise");
        habit.setFrequency(Frequency.DAILY);
        habit.setUser(user);
        habit.setCategory(category);
        habit.setStreak(streak);
        habit.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateHabit() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(streakRepository.findById(streakId)).thenReturn(Optional.of(streak));
        when(habitRepository.save(any(Habit.class))).thenReturn(habit);

        HabitResponseDto response = habitService.createHabit(userId, requestDto);

        assertNotNull(response);
        assertEquals("Workout", response.name());
        verify(habitRepository).save(any(Habit.class));
    }

    @Test
    void testGetHabitsForUser() {
        when(habitRepository.findByUserUuid(userId)).thenReturn(List.of(habit));

        List<HabitResponseDto> habits = habitService.getHabitsForUser(userId);

        assertEquals(1, habits.size());
        assertEquals("Workout", habits.getFirst().name());
    }

    @Test
    void testGetHabitById_found() {
        when(habitRepository.findById(habitId)).thenReturn(Optional.of(habit));

        HabitResponseDto dto = habitService.getHabitById(habitId);

        assertEquals(habitId, dto.uuid());
    }

    @Test
    void testGetHabitById_notFound() {
        when(habitRepository.findById(habitId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> habitService.getHabitById(habitId));
    }

    @Test
    void testDeleteHabit() {
        habitService.deleteHabit(habitId);
        verify(habitRepository).deleteById(habitId);
    }
}
