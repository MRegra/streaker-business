package com.streaker.controller.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CategoryRequestDto(

        @NotBlank(message = "Name must be specified")
        String name,

        @NotBlank(message = "Color must be specified")
        @Pattern(
                regexp = "^#(?:[0-9a-fA-F]{3}){1,2}$",
                message = "Color must be a valid hex code (e.g., #FF5733)"
        )
        String color

) {}
