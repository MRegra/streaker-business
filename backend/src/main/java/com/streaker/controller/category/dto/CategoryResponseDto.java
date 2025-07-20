package com.streaker.controller.category.dto;

import java.util.UUID;

public record CategoryResponseDto(
        UUID uuid,
        String name,
        String color
) {
}
