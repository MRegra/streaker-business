package com.streaker.controller.reward.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RewardRequestDto(

        @NotBlank(message = "Name must be specified")
        String name,

        @Size(max = 200, message = "Description's maximum length is 200 chars.")
        String description,

        @Min(value = 1, message = "Points required must be greater than zero")
        int pointsRequired
) {
}
