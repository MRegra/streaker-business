package com.streaker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.controller.reward.dto.RewardRequestDto;
import com.streaker.controller.reward.dto.RewardResponseDto;
import com.streaker.service.RewardService;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testadmin", roles = {"USER"})
public class RewardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RewardService rewardService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId, rewardId;
    private RewardRequestDto requestDto;
    private RewardResponseDto responseDto;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        rewardId = UUID.randomUUID();

        requestDto = new RewardRequestDto("Meditation Time", "15 min break", 5);
        responseDto = new RewardResponseDto(
                rewardId,
                "Meditation Time",
                "15 min break",
                5,
                false,
                null,
                userId
        );
    }

    @Test
    void testCreateReward() throws Exception {
        Mockito.when(rewardService.createReward(any(), any())).thenReturn(responseDto);

        mockMvc.perform(post("/users/{userId}/rewards", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(rewardId.toString()))
                .andExpect(jsonPath("$.pointsRequired").value(5));
    }

    @Test
    void testGetRewardsByUser() throws Exception {
        Mockito.when(rewardService.getRewardsByUser(userId)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/users/{userId}/rewards", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].uuid").value(rewardId.toString()));
    }

    @Test
    void testUnlockReward() throws Exception {
        RewardResponseDto unlocked = new RewardResponseDto(
                rewardId,
                "Meditation Time",
                "15 min break",
                5,
                true,
                Instant.now(),
                userId
        );

        Mockito.when(rewardService.unlockReward(rewardId)).thenReturn(unlocked);

        mockMvc.perform(post("/users/{userId}/rewards/{rewardId}/unlock", userId, rewardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unlocked").value(true))
                .andExpect(jsonPath("$.uuid").value(rewardId.toString()));
    }
}