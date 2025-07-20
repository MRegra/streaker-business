package com.streaker.service;

import com.streaker.controller.streak.dto.StreakDto;
import com.streaker.model.Streak;
import com.streaker.repository.StreakRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class StreakServiceImplTest {

    @Mock private StreakRepository streakRepository;
    @InjectMocks
    private StreakServiceImpl streakService;

    private UUID streakId;
    private UUID userId;
    private Streak streak;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        streakId = UUID.randomUUID();
        userId = UUID.randomUUID();

        streak = new Streak();
        streak.setUuid(streakId);
        streak.setStartDate(LocalDate.of(2025, 1, 1));
        streak.setCurrentCount(5);
        streak.setLongestStreak(10);
        streak.setIsActive(true);
    }

    @Test
    void testGetStreak_success() {
        when(streakRepository.findById(streakId)).thenReturn(Optional.of(streak));

        StreakDto dto = streakService.getStreak(streakId);

        assertEquals(streakId, dto.getUuid());
        assertEquals(5, dto.getCurrentCount());
        assertTrue(dto.isActive());
    }

    @Test
    void testGetStreak_notFound() {
        when(streakRepository.findById(streakId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> streakService.getStreak(streakId));
    }

    @Test
    void testGetStreaksByUser() {
        when(streakRepository.findByUserUuid(userId)).thenReturn(List.of(streak));

        List<StreakDto> streaks = streakService.getStreaksByUser(userId);

        assertEquals(1, streaks.size());
        assertEquals(streakId, streaks.getFirst().getUuid());
    }
}
