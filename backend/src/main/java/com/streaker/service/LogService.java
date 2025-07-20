package com.streaker.service;

import com.streaker.controller.log.dto.LogRequestDto;
import com.streaker.controller.log.dto.LogResponseDto;

import java.util.UUID;
import java.util.List;

public interface LogService {
    LogResponseDto createLog(UUID habitId, LogRequestDto dto);
    List<LogResponseDto> getLogsByHabit(UUID habitId);
    LogResponseDto getLog(UUID logId);
    LogResponseDto markLogCompleted(UUID logId);
}