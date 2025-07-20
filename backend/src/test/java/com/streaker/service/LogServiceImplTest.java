package com.streaker.service;

import com.streaker.controller.log.dto.LogRequestDto;
import com.streaker.controller.log.dto.LogResponseDto;
import com.streaker.exception.ResourceNotFoundException;
import com.streaker.model.Habit;
import com.streaker.model.Log;
import com.streaker.repository.HabitRepository;
import com.streaker.repository.LogRepository;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LogServiceImplTest {

    @Mock private LogRepository logRepository;
    @Mock private HabitRepository habitRepository;
    @InjectMocks private LogServiceImpl logService;

    private UUID habitId, logId;
    private Habit habit;
    private Log log;
    private LogRequestDto requestDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        habitId = UUID.randomUUID();
        logId = UUID.randomUUID();

        habit = new Habit();
        habit.setUuid(habitId);

        log = new Log();
        log.setUuid(logId);
        log.setHabit(habit);
        log.setDate(LocalDate.of(2025, 7, 20));
        log.setCompleted(true);

        requestDto = new LogRequestDto(LocalDate.of(2025, 7, 20), true);
    }

    @Test
    void testCreateLog() {
        when(habitRepository.findById(habitId)).thenReturn(Optional.of(habit));
        when(logRepository.save(any(Log.class))).thenReturn(log);

        LogResponseDto response = logService.createLog(habitId, requestDto);

        assertNotNull(response);
        assertEquals(logId, response.uuid());
        assertTrue(response.completed());
    }

    @Test
    void testGetLogsByHabit() {
        when(logRepository.findByHabitUuid(habitId)).thenReturn(List.of(log));

        List<LogResponseDto> logs = logService.getLogsByHabit(habitId);

        assertEquals(1, logs.size());
        assertEquals(logId, logs.getFirst().uuid());
    }

    @Test
    void testGetLog_success() {
        when(logRepository.findById(logId)).thenReturn(Optional.of(log));

        LogResponseDto response = logService.getLog(logId);

        assertEquals(logId, response.uuid());
        assertEquals(habitId, response.habitId());
    }

    @Test
    void testGetLog_notFound() {
        when(logRepository.findById(logId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> logService.getLog(logId));
    }

    @Test
    void testMarkLogCompleted() {
        log.setCompleted(false);
        when(logRepository.findById(logId)).thenReturn(Optional.of(log));
        when(logRepository.save(any(Log.class))).thenReturn(log);

        LogResponseDto result = logService.markLogCompleted(logId);

        assertTrue(result.completed());
        verify(logRepository).save(any(Log.class));
    }
}
