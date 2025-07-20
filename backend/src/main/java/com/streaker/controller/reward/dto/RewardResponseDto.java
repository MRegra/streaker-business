package com.streaker.controller.reward.dto;

import java.time.Instant;
import java.util.UUID;

public record RewardResponseDto(
        UUID uuid,
        String name,
        String description,
        int pointsRequired,
        boolean unlocked,
        Instant unlockedAt,  // Nullable field
        UUID userId
) {}