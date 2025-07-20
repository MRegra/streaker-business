package com.streaker.service;

import com.streaker.controller.reward.dto.RewardRequestDto;
import com.streaker.controller.reward.dto.RewardResponseDto;
import com.streaker.exception.ResourceNotFoundException;
import com.streaker.model.Reward;
import com.streaker.model.User;
import com.streaker.repository.RewardRepository;
import com.streaker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RewardServiceImpl implements RewardService {

    private final RewardRepository rewardRepository;
    private final UserRepository userRepository;

    @Override
    public RewardResponseDto createReward(UUID userId, RewardRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Reward reward = new Reward();
        reward.setName(dto.name());
        reward.setDescription(dto.description());
        reward.setPointsRequired(dto.pointsRequired());
        reward.setUser(user);

        return mapToDto(rewardRepository.save(reward));
    }

    @Override
    public List<RewardResponseDto> getRewardsByUser(UUID userId) {
        return rewardRepository.findByUserUuid(userId).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public RewardResponseDto unlockReward(UUID rewardId) {
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new ResourceNotFoundException("Reward not found"));

        reward.setUnlocked(true);
        reward.setUnlockedAt(Instant.now());

        return mapToDto(rewardRepository.save(reward));
    }

    private RewardResponseDto mapToDto(Reward reward) {
        return new RewardResponseDto(
                reward.getUuid(),
                reward.getName(),
                reward.getDescription(),
                reward.getPointsRequired(),
                reward.isUnlocked(),
                reward.getUnlockedAt() != null ? reward.getUnlockedAt() : null,
                reward.getUser().getUuid()
        );
    }
}
