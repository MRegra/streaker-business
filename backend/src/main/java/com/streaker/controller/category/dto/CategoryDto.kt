package com.streaker.controller.category.dto

import java.util.UUID

data class CategoryDto(
    val uuid: UUID,
    val name: String,
    val color: String
)
