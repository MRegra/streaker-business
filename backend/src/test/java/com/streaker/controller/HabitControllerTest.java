package com.streaker.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.controller.habit.HabitController;
import com.streaker.controller.habit.dto.HabitRequestDto;
import com.streaker.controller.habit.dto.HabitResponseDto;
import com.streaker.service.HabitService;
import com.streaker.utlis.enums.Frequency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HabitController.class)
public class HabitControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private HabitService habitService;
    @Autowired private ObjectMapper objectMapper;

    private UUID userId;
    private UUID habitId;
    private HabitRequestDto requestDto;
    private HabitResponseDto responseDto;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        habitId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID streakId = UUID.randomUUID();

        requestDto = new HabitRequestDto("Read", "Read 30min", Frequency.DAILY, categoryId, streakId);
        responseDto = new HabitResponseDto(habitId, "Read", "Read 30min", Frequency.DAILY, LocalDateTime.now(), userId, categoryId, streakId);
    }

    @Test
    void testCreateHabit() throws Exception {
        Mockito.when(habitService.createHabit(any(), any())).thenReturn(responseDto);

        mockMvc.perform(post("/users/{userId}/habits", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Read"));
    }

    @Test
    void testGetHabitsForUser() throws Exception {
        Mockito.when(habitService.getHabitsForUser(userId)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/users/{userId}/habits", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetHabitById() throws Exception {
        Mockito.when(habitService.getHabitById(habitId)).thenReturn(responseDto);

        mockMvc.perform(get("/users/{userId}/habits/{habitId}", userId, habitId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(habitId.toString()));
    }

    @Test
    void testDeleteHabit() throws Exception {
        mockMvc.perform(delete("/users/{userId}/habits/{habitId}", userId, habitId))
                .andExpect(status().isNoContent());
    }
}
