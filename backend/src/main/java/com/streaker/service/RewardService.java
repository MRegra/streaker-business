package com.streaker.service;

import com.streaker.controller.reward.dto.RewardRequestDto;
import com.streaker.controller.reward.dto.RewardResponseDto;

import java.util.UUID;
import java.util.List;

public interface RewardService {

    RewardResponseDto createReward(UUID userId, RewardRequestDto dto);
    List<RewardResponseDto> getRewardsByUser(UUID userId);
    RewardResponseDto unlockReward(UUID rewardId);
}