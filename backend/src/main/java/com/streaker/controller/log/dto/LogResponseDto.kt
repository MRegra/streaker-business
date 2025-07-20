package com.streaker.controller.log.dto

import java.time.LocalDate
import java.util.UUID

data class LogResponseDto(
    val uuid: UUID,
    val date: LocalDate,
    val completed: Boolean,
    val habitId: UUID
)
