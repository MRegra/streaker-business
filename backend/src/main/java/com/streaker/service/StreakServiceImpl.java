package com.streaker.service;

import com.streaker.controller.streak.dto.StreakDto;
import com.streaker.model.Streak;
import com.streaker.repository.StreakRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StreakServiceImpl implements StreakService {

    private final StreakRepository streakRepository;

    @Override
    public StreakDto getStreak(UUID id) {
        return streakRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Streak not found"));
    }

    @Override
    public List<StreakDto> getStreaksByUser(UUID userId) {
        return streakRepository.findByUserUuid(userId).stream()
                .map(this::mapToDto)
                .toList();
    }

    private StreakDto mapToDto(Streak streak) {
        return new StreakDto(
                streak.getUuid(),
                streak.getStartDate(),
                streak.getCurrentCount(),
                streak.getLongestStreak(),
                streak.getIsActive()
        );
    }
}
