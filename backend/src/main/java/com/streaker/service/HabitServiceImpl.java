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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HabitServiceImpl implements HabitService {

    private final HabitRepository habitRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final StreakRepository streakRepository;

    @Override
    public HabitResponseDto createHabit(UUID userId, HabitRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Streak streak = streakRepository.findById(dto.streakId())
                .orElseThrow(() -> new ResourceNotFoundException("Streak not found"));

        Habit habit = new Habit();
        habit.setName(dto.name());
        habit.setDescription(dto.description());
        habit.setFrequency(dto.frequency());
        habit.setUser(user);
        habit.setCategory(category);
        habit.setStreak(streak);

        Habit saved = habitRepository.save(habit);
        return mapToDto(saved);
    }

    @Override
    public List<HabitResponseDto> getHabitsForUser(UUID userId) {
        return habitRepository.findByUserUuid(userId).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public HabitResponseDto getHabitById(UUID habitId) {
        return habitRepository.findById(habitId)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Habit not found"));
    }

    @Override
    public void deleteHabit(UUID habitId) {
        habitRepository.deleteById(habitId);
    }

    private HabitResponseDto mapToDto(Habit habit) {
        return new HabitResponseDto(
                habit.getUuid(),
                habit.getName(),
                habit.getDescription(),
                habit.getFrequency(),
                habit.getCreatedAt(),
                habit.getUser().getUuid(),
                habit.getCategory().getUuid(),
                habit.getStreak().getUuid()
        );
    }
}
