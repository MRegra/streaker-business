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
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
class LogServiceImplTest {

    @Mock
    private LogRepository logRepository;

    @Mock
    private HabitRepository habitRepository;

    @InjectMocks
    private LogServiceImpl logService;

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
    void createLog_shouldReturnResponseDto_whenHabitExists() {
        when(habitRepository.findById(habitId)).thenReturn(Optional.of(habit));
        when(logRepository.save(any(Log.class))).thenReturn(log);

        LogResponseDto response = logService.createLog(habitId, requestDto);

        assertAll("Log creation",
                () -> assertNotNull(response),
                () -> assertEquals(logId, response.uuid()),
                () -> assertTrue(response.completed()),
                () -> assertEquals(LocalDate.of(2025, 7, 20), response.date())
        );

        verify(habitRepository).findById(habitId);
        verify(logRepository).save(any(Log.class));
    }

    @Test
    void createLog_shouldThrow_whenHabitNotFound() {
        when(habitRepository.findById(habitId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                logService.createLog(habitId, requestDto));
    }

    @Test
    void getLogsByHabit_shouldReturnListOfResponses() {
        when(logRepository.findByHabitUuid(habitId)).thenReturn(List.of(log));

        List<LogResponseDto> logs = logService.getLogsByHabit(habitId);

        assertAll("Log list",
                () -> assertEquals(1, logs.size()),
                () -> assertEquals(logId, logs.getFirst().uuid()),
                () -> assertEquals(habitId, logs.getFirst().habitId())
        );
    }

    @Test
    void getLog_shouldReturnResponse_whenLogExists() {
        when(logRepository.findById(logId)).thenReturn(Optional.of(log));

        LogResponseDto response = logService.getLog(logId);

        assertAll("Single log retrieval",
                () -> assertEquals(logId, response.uuid()),
                () -> assertEquals(habitId, response.habitId()),
                () -> assertTrue(response.completed())
        );
    }

    @Test
    void getLog_shouldThrow_whenLogNotFound() {
        when(logRepository.findById(logId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> logService.getLog(logId));
    }

    @Test
    void markLogCompleted_shouldSetCompletedTrue_andSave() {
        log.setCompleted(false);
        when(logRepository.findById(logId)).thenReturn(Optional.of(log));
        when(logRepository.save(any(Log.class))).thenReturn(log);

        LogResponseDto result = logService.markLogCompleted(logId);

        assertAll("Mark completed",
                () -> assertTrue(result.completed()),
                () -> assertEquals(logId, result.uuid())
        );

        verify(logRepository).save(any(Log.class));
    }
}
