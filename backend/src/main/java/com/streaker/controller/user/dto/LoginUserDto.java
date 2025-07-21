package com.streaker.controller.user.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginUserDto(

        @NotBlank(message = "Username is required")
        String username,

        String password
) {
}
