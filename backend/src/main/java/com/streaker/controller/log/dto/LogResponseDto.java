package com.streaker.controller.log.dto;

import java.time.LocalDate;
import java.util.UUID;

public record LogResponseDto(
        UUID uuid,
        LocalDate date,
        boolean completed,
        UUID habitId
) {}
