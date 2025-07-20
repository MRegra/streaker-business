package com.streaker.controller.streak;

import com.streaker.controller.streak.dto.StreakDto;
import com.streaker.service.StreakService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/{userId}/streaks")
@RequiredArgsConstructor
@Tag(name = "Streak", description = "View user streaks")
public class StreakController {

    private final StreakService streakService;

    @Operation(summary = "Get all streaks for a user")
    @GetMapping
    public ResponseEntity<List<StreakDto>> getStreaks(@PathVariable UUID userId) {
        return ResponseEntity.ok(streakService.getStreaksByUser(userId));
    }

    @Operation(summary = "Get a specific streak")
    @GetMapping("/{id}")
    public ResponseEntity<StreakDto> getStreak(@PathVariable UUID id) {
        return ResponseEntity.ok(streakService.getStreak(id));
    }
}
