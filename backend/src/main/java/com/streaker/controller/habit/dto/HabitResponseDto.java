package com.streaker.controller.habit.dto;


import com.streaker.utlis.enums.Frequency;

import java.time.LocalDateTime;
import java.util.UUID;

public record HabitResponseDto(
        UUID uuid,
        String name,
        String description,
        Frequency frequency,
        LocalDateTime createdAt,
        UUID userId,
        UUID categoryId,
        UUID streakId
) {}
