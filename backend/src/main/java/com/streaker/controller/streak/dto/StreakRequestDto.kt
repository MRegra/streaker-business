package com.streaker.controller.streak.dto

import java.time.LocalDate

data class StreakRequestDto(
    val startDate: LocalDate = LocalDate.now()
)
