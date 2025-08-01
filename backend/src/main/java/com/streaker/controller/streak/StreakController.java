package com.streaker.controller.streak;

import com.streaker.config.JwtAuthorizationValidator;
import com.streaker.controller.streak.dto.StreakDto;
import com.streaker.service.StreakService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/users/{userId}/streaks")
@RequiredArgsConstructor
@Tag(name = "Streak", description = "View user streaks")
@SecurityRequirement(name = "bearerAuth")
public class StreakController {

    private final JwtAuthorizationValidator jwtAuthorizationValidator;

    private final StreakService streakService;

    @Operation(summary = "Get all streaks for a user")
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<StreakDto>> getStreaks(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID userId) {
        jwtAuthorizationValidator.validateToken(authHeader, userId);
        return ResponseEntity.ok(streakService.getStreaksByUser(userId));
    }

    @Operation(summary = "Get a specific streak")
    @GetMapping("/{streakId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<StreakDto> getStreak(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID userId,
            @PathVariable UUID streakId) {
        jwtAuthorizationValidator.validateToken(authHeader, userId);
        return ResponseEntity.ok(streakService.getStreak(streakId));
    }
}
