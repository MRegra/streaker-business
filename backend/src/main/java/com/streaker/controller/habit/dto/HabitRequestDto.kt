package com.streaker.controller.habit.dto

import com.streaker.utlis.enums.Frequency
import java.util.UUID

class HabitRequestDto(
    val name: String,
    val description: String,
    val frequency: Frequency,
    val categoryId: UUID,
    val streakId: UUID
) {}