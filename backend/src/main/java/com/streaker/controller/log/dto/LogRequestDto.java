package com.streaker.controller.log.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record LogRequestDto(

        @NotNull(message = "Date must be specified")
        @PastOrPresent(message = "Date cannot be in the future")
        LocalDate date,

        @NotNull(message = "Completion status must be specified")
        Boolean completed
) {
}
