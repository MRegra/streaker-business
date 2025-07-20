package com.streaker.controller.streak.dto;

import java.time.LocalDate;
import java.util.UUID;

public record StreakDto(
        UUID uuid,
        LocalDate startDate,
        int currentCount,
        int longestStreak,
        boolean isActive
) {}