package com.streaker.controller.reward.dto

import java.time.Instant
import java.util.UUID

data class RewardResponseDto(
    val uuid: UUID,
    val name: String,
    val description: String,
    val pointsRequired: Int,
    val unlocked: Boolean,
    var unlockedAt: Instant?,
    val userId: UUID
)
