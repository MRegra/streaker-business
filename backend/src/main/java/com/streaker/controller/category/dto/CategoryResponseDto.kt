package com.streaker.controller.category.dto

import java.util.UUID

data class CategoryResponseDto(
    val uuid: UUID,
    val name: String,
    val color: String
)
