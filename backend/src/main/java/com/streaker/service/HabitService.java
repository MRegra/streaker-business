package com.streaker.service;

import com.streaker.controller.habit.dto.HabitRequestDto;
import com.streaker.controller.habit.dto.HabitResponseDto;

import java.util.List;
import java.util.UUID;

public interface HabitService {
    HabitResponseDto createHabit(UUID userId, HabitRequestDto dto);
    List<HabitResponseDto> getHabitsForUser(UUID userId);
    HabitResponseDto getHabitById(UUID habitId);
    void deleteHabit(UUID habitId);
}
