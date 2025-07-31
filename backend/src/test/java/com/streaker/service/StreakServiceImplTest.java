package com.streaker.service;

import com.streaker.controller.streak.dto.StreakDto;
import com.streaker.exception.ResourceNotFoundException;
import com.streaker.model.Streak;
import com.streaker.repository.StreakRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
class StreakServiceImplTest {

    @Mock
    private StreakRepository streakRepository;

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
    void shouldReturnStreak_whenStreakExists() {
        when(streakRepository.findById(streakId)).thenReturn(Optional.of(streak));

        StreakDto dto = streakService.getStreak(streakId);

        assertAll("Valid streak DTO",
                () -> assertEquals(streakId, dto.uuid()),
                () -> assertEquals(5, dto.currentCount()),
                () -> assertEquals(10, dto.longestStreak()),
                () -> assertTrue(dto.isActive())
        );

        verify(streakRepository).findById(streakId);
    }

    @Test
    void shouldThrow_whenStreakNotFound() {
        when(streakRepository.findById(streakId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> streakService.getStreak(streakId));

        verify(streakRepository).findById(streakId);
    }

    @Test
    void shouldReturnStreaksByUser_whenUserHasStreaks() {
        when(streakRepository.findByUserUuid(userId)).thenReturn(List.of(streak));

        List<StreakDto> result = streakService.getStreaksByUser(userId);

        assertAll("Streaks by user",
                () -> assertEquals(1, result.size()),
                () -> assertEquals(streakId, result.getFirst().uuid()),
                () -> assertEquals(5, result.getFirst().currentCount())
        );

        verify(streakRepository).findByUserUuid(userId);
    }

    @Test
    void shouldReturnEmptyList_whenUserHasNoStreaks() {
        when(streakRepository.findByUserUuid(userId)).thenReturn(List.of());

        List<StreakDto> result = streakService.getStreaksByUser(userId);

        assertTrue(result.isEmpty());
        verify(streakRepository).findByUserUuid(userId);
    }
}
