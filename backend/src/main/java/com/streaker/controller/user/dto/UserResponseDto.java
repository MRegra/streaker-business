package com.streaker.controller.user.dto;

import java.util.UUID;

public record UserResponseDto(
        UUID uuid,
        String username,
        String email
) {}