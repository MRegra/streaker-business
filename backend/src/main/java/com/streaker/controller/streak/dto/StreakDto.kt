package com.streaker.controller.streak.dto

import java.time.LocalDate
import java.util.UUID

data class StreakDto(
    val uuid: UUID,
    val startDate: LocalDate,
    val currentCount: Int,
    val longestStreak: Int,
    val isActive: Boolean
)
