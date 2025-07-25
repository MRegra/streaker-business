package com.streaker.controller.habit;

import com.streaker.controller.habit.dto.HabitRequestDto;
import com.streaker.controller.habit.dto.HabitResponseDto;
import com.streaker.service.HabitService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/users/{userId}/habits")
@RequiredArgsConstructor
@Tag(name = "Habit", description = "Manage user habits")
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "HabitService is a Spring-managed bean and safe to inject")
@SecurityRequirement(name = "bearerAuth")
public class HabitController {

    private final HabitService habitService;

    @Operation(summary = "Create a new habit for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Habit created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
    })
    @PostMapping
    public ResponseEntity<HabitResponseDto> createHabit(
            @PathVariable UUID userId,
            @Valid @RequestBody HabitRequestDto dto) {
        return ResponseEntity.ok(habitService.createHabit(userId, dto));
    }

    @Operation(summary = "Get all habits for a user")
    @ApiResponse(responseCode = "200", description = "List of habits")
    @GetMapping
    public ResponseEntity<List<HabitResponseDto>> getHabitsForUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(habitService.getHabitsForUser(userId));
    }

    @Operation(summary = "Get a specific habit by ID")
    @GetMapping("/{habitId}")
    public ResponseEntity<HabitResponseDto> getHabitById(@PathVariable UUID habitId) {
        return ResponseEntity.ok(habitService.getHabitById(habitId));
    }

    @Operation(summary = "Delete a habit by ID")
    @DeleteMapping("/{habitId}")
    public ResponseEntity<Void> deleteHabit(@PathVariable UUID habitId) {
        habitService.deleteHabit(habitId);
        return ResponseEntity.noContent().build();
    }
}
