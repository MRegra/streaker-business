package com.streaker.controller.reward.dto

data class RewardRequestDto(
    val name: String,
    val description: String,
    val pointsRequired: Int
)
