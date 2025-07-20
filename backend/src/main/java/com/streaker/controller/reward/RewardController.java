package com.streaker.controller.reward;

import com.streaker.controller.reward.dto.RewardRequestDto;
import com.streaker.controller.reward.dto.RewardResponseDto;
import com.streaker.service.RewardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/{userId}/rewards")
@RequiredArgsConstructor
@Tag(name = "Reward", description = "Manage user rewards")
public class RewardController {

    private final RewardService rewardService;

    @Operation(summary = "Create a new reward for a user")
    @PostMapping
    public ResponseEntity<RewardResponseDto> createReward(
            @PathVariable UUID userId,
            @Valid @RequestBody RewardRequestDto dto) {
        return ResponseEntity.ok(rewardService.createReward(userId, dto));
    }

    @Operation(summary = "Get all rewards for a user")
    @GetMapping
    public ResponseEntity<List<RewardResponseDto>> getRewards(@PathVariable UUID userId) {
        return ResponseEntity.ok(rewardService.getRewardsByUser(userId));
    }

    @Operation(summary = "Unlock a reward")
    @PostMapping("/{rewardId}/unlock")
    public ResponseEntity<RewardResponseDto> unlockReward(
            @PathVariable UUID rewardId) {
        return ResponseEntity.ok(rewardService.unlockReward(rewardId));
    }
}
