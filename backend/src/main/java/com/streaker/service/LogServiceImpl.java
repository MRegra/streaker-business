package com.streaker.service;

import com.streaker.controller.log.dto.LogRequestDto;
import com.streaker.controller.log.dto.LogResponseDto;
import com.streaker.model.Habit;
import com.streaker.model.Log;
import com.streaker.repository.HabitRepository;
import com.streaker.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final LogRepository logRepository;
    private final HabitRepository habitRepository;

    @Override
    public LogResponseDto createLog(UUID habitId, LogRequestDto dto) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found"));

        Log log = new Log();
        log.setDate(dto.getDate());
        log.setCompleted(dto.getCompleted() && dto.getCompleted());
        log.setHabit(habit);

        return mapToDto(logRepository.save(log));
    }

    @Override
    public List<LogResponseDto> getLogsByHabit(UUID habitId) {
        return logRepository.findByHabitUuid(habitId).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public LogResponseDto getLog(UUID logId) {
        return logRepository.findById(logId)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Log not found"));
    }

    @Override
    public LogResponseDto markLogCompleted(UUID logId) {
        Log log = logRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("Log not found"));

        log.setCompleted(true);
        return mapToDto(logRepository.save(log));
    }

    private LogResponseDto mapToDto(Log log) {
        return new LogResponseDto(
                log.getUuid(),
                log.getDate(),
                log.getCompleted(),
                log.getHabit().getUuid()
        );
    }
}