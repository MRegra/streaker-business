package com.streaker.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.controller.streak.StreakController;
import com.streaker.controller.streak.dto.StreakDto;
import com.streaker.service.StreakService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StreakController.class)
public class StreakControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private StreakService streakService;
    @Autowired private ObjectMapper objectMapper;

    private UUID userId, streakId;
    private StreakDto streakDto;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        streakId = UUID.randomUUID();
        streakDto = new StreakDto(
                streakId,
                LocalDate.of(2024, 1, 1),
                3,
                10,
                true
        );
    }

    @Test
    void testGetStreaksByUser() throws Exception {
        Mockito.when(streakService.getStreaksByUser(userId)).thenReturn(List.of(streakDto));

        mockMvc.perform(get("/users/{userId}/streaks", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].uuid").value(streakId.toString()));
    }

    @Test
    void testGetStreakById() throws Exception {
        Mockito.when(streakService.getStreak(streakId)).thenReturn(streakDto);

        mockMvc.perform(get("/users/{userId}/streaks/{id}", userId, streakId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(streakId.toString()))
                .andExpect(jsonPath("$.currentCount").value(3))
                .andExpect(jsonPath("$.isActive").value(true));
    }
}
