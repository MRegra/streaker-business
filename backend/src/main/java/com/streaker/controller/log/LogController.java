package com.streaker.controller.log;

import com.streaker.config.JwtAuthorizationValidator;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/users/{userId}")
@RequiredArgsConstructor
@Tag(name = "Log", description = "Track habit logs")
@SecurityRequirement(name = "bearerAuth")
public class LogController {

    private final JwtAuthorizationValidator jwtAuthorizationValidator;
    private final LogService logService;

    @Operation(summary = "Create a log entry for a habit")
    @PostMapping("/habits/{habitId}/logs")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<LogResponseDto> createLog(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID userId,
            @PathVariable UUID habitId,
            @Valid @RequestBody LogRequestDto dto) {
        jwtAuthorizationValidator.validateToken(authHeader, userId);
        return ResponseEntity.ok(logService.createLog(habitId, dto));
    }

    @Operation(summary = "Get all logs for a habit")
    @GetMapping("/habits/{habitId}/logs")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<LogResponseDto>> getLogs(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID userId,
            @PathVariable UUID habitId) {
        jwtAuthorizationValidator.validateToken(authHeader, userId);
        return ResponseEntity.ok(logService.getLogsByHabit(habitId));
    }

    @Operation(summary = "Get a specific log entry")
    @GetMapping("/logs/{logId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<LogResponseDto> getLog(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID userId,
            @PathVariable UUID habitId,
            @PathVariable UUID logId) {
        jwtAuthorizationValidator.validateToken(authHeader, userId);
        return ResponseEntity.ok(logService.getLog(logId));
    }

    @Operation(summary = "Mark a log as completed")
    @PostMapping("/logs/{logId}/complete")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<LogResponseDto> markCompleted(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID userId,
            @PathVariable UUID logId) {
        jwtAuthorizationValidator.validateToken(authHeader, userId);
        return ResponseEntity.ok(logService.markLogCompleted(logId));
    }
}
