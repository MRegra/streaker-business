package com.streaker.controller.habit.dto;

import com.streaker.utlis.enums.Frequency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record HabitRequestDto(
        @Schema(description = "The title of the habit", example = "Workout", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Name must be specified")
        String name,

        @Schema(description = "The title of the habit", example = "This is a description", requiredMode = Schema.RequiredMode.REQUIRED)
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
