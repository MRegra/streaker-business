package com.streaker.controller.habit.dto

import com.streaker.utlis.enums.Frequency
import java.time.LocalDateTime
import java.util.UUID

class HabitResponseDto(
    val uuid: UUID,
    val name: String,
    val description: String,
    val frequency: Frequency,
    val createdAt: LocalDateTime,
    val userId: UUID,
    val categoryId: UUID,
    val streakId: UUID
) {}