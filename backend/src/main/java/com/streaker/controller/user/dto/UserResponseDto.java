package com.streaker.controller.user.dto;

import com.streaker.utlis.enums.Role;

import java.util.UUID;

public record UserResponseDto(
        UUID uuid,
        String username,
        String email,
        Role role
) {}