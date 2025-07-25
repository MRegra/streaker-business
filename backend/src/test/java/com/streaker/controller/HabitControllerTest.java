package com.streaker.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.PostgresTestContainerConfig;
import com.streaker.controller.habit.dto.HabitRequestDto;
import com.streaker.controller.habit.dto.HabitResponseDto;
import com.streaker.service.HabitService;
import com.streaker.utlis.enums.Frequency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "testadmin", roles = {"USER"})
public class HabitControllerTest extends PostgresTestContainerConfig {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HabitService habitService;

    @Autowired
    private ObjectMapper objectMapper;

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

        mockMvc.perform(post("/v1/users/{userId}/habits", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Read"));
    }

    @Test
    void testGetHabitsForUser() throws Exception {
        Mockito.when(habitService.getHabitsForUser(userId)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/v1/users/{userId}/habits", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetHabitById() throws Exception {
        Mockito.when(habitService.getHabitById(habitId)).thenReturn(responseDto);

        mockMvc.perform(get("/v1/users/{userId}/habits/{habitId}", userId, habitId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(habitId.toString()));
    }

    @Test
    void testDeleteHabit() throws Exception {
        mockMvc.perform(delete("/v1/users/{userId}/habits/{habitId}", userId, habitId))
                .andExpect(status().isNoContent());
    }
}
