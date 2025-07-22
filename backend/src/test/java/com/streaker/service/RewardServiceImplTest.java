package com.streaker.service;

import com.streaker.controller.reward.dto.RewardRequestDto;
import com.streaker.controller.reward.dto.RewardResponseDto;
import com.streaker.exception.ResourceNotFoundException;
import com.streaker.model.Reward;
import com.streaker.model.User;
import com.streaker.repository.RewardRepository;
import com.streaker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
public class RewardServiceImplTest {

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RewardServiceImpl rewardService;

    private UUID userId, rewardId;
    private Reward reward;
    private RewardRequestDto requestDto;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID();
        rewardId = UUID.randomUUID();

        user = new User();
        user.setUuid(userId);

        reward = new Reward();
        reward.setUuid(rewardId);
        reward.setName("Free Day");
        reward.setDescription("Take a day off");
        reward.setPointsRequired(10);
        reward.setUser(user);
        reward.setUnlocked(false);
        reward.setUnlockedAt(null);

        requestDto = new RewardRequestDto("Free Day", "Take a day off", 10);
    }

    @Test
    void testCreateReward() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(rewardRepository.save(any(Reward.class))).thenReturn(reward);

        RewardResponseDto response = rewardService.createReward(userId, requestDto);

        assertNotNull(response);
        assertEquals("Free Day", response.name());
        assertFalse(response.unlocked());
    }

    @Test
    void testGetRewardsByUser() {
        when(rewardRepository.findByUserUuid(userId)).thenReturn(List.of(reward));

        List<RewardResponseDto> rewards = rewardService.getRewardsByUser(userId);

        assertEquals(1, rewards.size());
        assertEquals(rewardId, rewards.getFirst().uuid());
    }

    @Test
    void testUnlockReward() {
        reward.setUnlocked(false);
        when(rewardRepository.findById(rewardId)).thenReturn(Optional.of(reward));
        when(rewardRepository.save(any(Reward.class))).thenAnswer(invocation -> {
            reward.setUnlocked(true);
            reward.setUnlockedAt(Instant.now());
            return reward;
        });

        RewardResponseDto result = rewardService.unlockReward(rewardId);

        assertTrue(result.unlocked());
        assertNotNull(result.unlockedAt());
        verify(rewardRepository).save(any(Reward.class));
    }

    @Test
    void testUnlockReward_notFound() {
        when(rewardRepository.findById(rewardId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> rewardService.unlockReward(rewardId));
    }
}
