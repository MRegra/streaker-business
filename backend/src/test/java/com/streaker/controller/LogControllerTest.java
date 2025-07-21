package com.streaker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.controller.log.dto.LogRequestDto;
import com.streaker.controller.log.dto.LogResponseDto;
import com.streaker.service.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testadmin", roles = {"USER"})
public class LogControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private LogService logService;
    @Autowired private ObjectMapper objectMapper;

    private UUID habitId, logId;
    private LogRequestDto requestDto;
    private LogResponseDto responseDto;

    @BeforeEach
    void setup() {
        habitId = UUID.randomUUID();
        logId = UUID.randomUUID();

        requestDto = new LogRequestDto(LocalDate.of(2025, 7, 20), true);
        responseDto = new LogResponseDto(logId, requestDto.date(), true, habitId);
    }

    @Test
    void testCreateLog() throws Exception {
        Mockito.when(logService.createLog(any(), any())).thenReturn(responseDto);

        mockMvc.perform(post("/habits/{habitId}/logs", habitId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(logId.toString()))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void testGetLogsByHabit() throws Exception {
        Mockito.when(logService.getLogsByHabit(habitId)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/habits/{habitId}/logs", habitId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].uuid").value(logId.toString()));
    }

    @Test
    void testGetLog() throws Exception {
        Mockito.when(logService.getLog(logId)).thenReturn(responseDto);

        mockMvc.perform(get("/habits/{habitId}/logs/{logId}", habitId, logId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(logId.toString()));
    }

    @Test
    void testMarkLogCompleted() throws Exception {
        Mockito.when(logService.markLogCompleted(logId)).thenReturn(responseDto);

        mockMvc.perform(post("/habits/{habitId}/logs/{logId}/complete", habitId, logId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
    }
}
