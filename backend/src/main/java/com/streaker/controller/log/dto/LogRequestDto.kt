package com.streaker.controller.log.dto

import java.time.LocalDate

data class LogRequestDto(
    val date: LocalDate,
    val completed: Boolean
)
