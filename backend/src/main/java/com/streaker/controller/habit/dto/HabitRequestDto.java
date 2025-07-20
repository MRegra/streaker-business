package com.streaker.controller.habit.dto;

import com.streaker.utlis.enums.Frequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record HabitRequestDto(
        @NotBlank(message = "Name must be specified")
        String name,

        @NotBlank(message = "Description must be specified")
        String description,

        @NotNull(message = "Frequency must be specified")
        Frequency frequency,

        @NotNull(message = "Category ID must be specified")
        UUID categoryId,

        @NotNull(message = "Streak ID must be specified")
        UUID streakId
) {
}
