package com.streaker.controller.log;

import com.streaker.controller.log.dto.LogRequestDto;
import com.streaker.controller.log.dto.LogResponseDto;
import com.streaker.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/users/habits/{habitId}/logs")
@RequiredArgsConstructor
@Tag(name = "Log", description = "Track habit logs")
@SecurityRequirement(name = "bearerAuth")
public class LogController {

    private final LogService logService;

    @Operation(summary = "Create a log entry for a habit")
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<LogResponseDto> createLog(@PathVariable UUID habitId, @Valid @RequestBody LogRequestDto dto) {
        return ResponseEntity.ok(logService.createLog(habitId, dto));
    }

    @Operation(summary = "Get all logs for a habit")
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<LogResponseDto>> getLogs(@PathVariable UUID habitId) {
        return ResponseEntity.ok(logService.getLogsByHabit(habitId));
    }

    @Operation(summary = "Get a specific log entry")
    @GetMapping("/{logId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<LogResponseDto> getLog(@PathVariable UUID logId) {
        return ResponseEntity.ok(logService.getLog(logId));
    }

    @Operation(summary = "Mark a log as completed")
    @PostMapping("/{logId}/complete")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<LogResponseDto> markCompleted(@PathVariable UUID logId) {
        return ResponseEntity.ok(logService.markLogCompleted(logId));
    }
}
