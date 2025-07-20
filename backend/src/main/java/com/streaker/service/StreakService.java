package com.streaker.service;

import com.streaker.controller.streak.dto.StreakDto;

import java.util.List;
import java.util.UUID;

public interface StreakService {

    StreakDto getStreak(UUID id);
    List<StreakDto> getStreaksByUser(UUID userId);

}
