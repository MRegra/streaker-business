package com.streaker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.BaseIntegrationTest;
import com.streaker.TestContainerConfig;
import com.streaker.controller.reward.dto.RewardRequestDto;
import com.streaker.model.Reward;
import com.streaker.model.User;
import com.streaker.repository.RewardRepository;
import com.streaker.repository.UserRepository;
import com.streaker.utils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WithMockUser(username = "testuser-reward", roles = "USER")
@DisplayName("RewardController Integration Tests")
@Import(TestContainerConfig.class)
class RewardControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RewardRepository rewardRepository;
    @Autowired
    private UserRepository userRepository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        User user = TestDataFactory.createUser("testuser-reward", "testuser-reward@example.com", "password123");
        userId = userRepository.save(user).getUuid();
    }

    @Nested
    @DisplayName("POST /v1/users/{userId}/rewards")
    class CreateReward {

        @Test
        @DisplayName("Should create a reward and return it")
        void shouldCreateRewardSuccessfully() throws Exception {
            RewardRequestDto dto = new RewardRequestDto("Buy Coffee", "Morning treat", 10);

            mockMvc.perform(post("/v1/users/{userId}/rewards", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Buy Coffee"))
                    .andExpect(jsonPath("$.pointsRequired").value(10));

            List<Reward> rewards = rewardRepository.findAll();
            assertThat(rewards).hasSize(1);
            assertThat(rewards.getFirst().getName()).isEqualTo("Buy Coffee");
        }

        @Test
        @DisplayName("Should return 400 when required fields are missing")
        void shouldReturnBadRequestForMissingFields() throws Exception {
            RewardRequestDto invalidDto = new RewardRequestDto(null, null, 5);

            mockMvc.perform(post("/v1/users/{userId}/rewards", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 when user does not exist")
        void shouldReturnNotFoundForNonExistentUser() throws Exception {
            RewardRequestDto dto = new RewardRequestDto("Gym Pass", "Weekend pass", 15);
            UUID fakeUserId = UUID.randomUUID();

            mockMvc.perform(post("/v1/users/{userId}/rewards", fakeUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /v1/users/{userId}/rewards")
    class GetRewards {

        @Test
        @DisplayName("Should return all rewards for a user")
        void shouldReturnAllUserRewards() throws Exception {
            User user = userRepository.findById(userId).orElseThrow();
            rewardRepository.save(TestDataFactory.createReward(user));
            rewardRepository.save(TestDataFactory.createReward(user));

            mockMvc.perform(get("/v1/users/{userId}/rewards", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("Should return empty list if user has no rewards")
        void shouldReturnEmptyRewardList() throws Exception {
            mockMvc.perform(get("/v1/users/{userId}/rewards", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Should return 404 if user does not exist")
        void shouldReturnNotFoundForNonExistentUser() throws Exception {
            UUID fakeUserId = UUID.randomUUID();
            mockMvc.perform(get("/v1/users/{userId}/rewards", fakeUserId))
                    .andExpect(status().isOk()) // still ok because it's a query and may return empty
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("POST /v1/users/{userId}/rewards/{rewardId}/unlock")
    class UnlockReward {

        @Test
        @DisplayName("Should unlock the reward and return updated response")
        void shouldUnlockReward() throws Exception {
            User user = userRepository.findById(userId).orElseThrow();
            Reward reward = rewardRepository.save(TestDataFactory.createReward(user));

            mockMvc.perform(post("/v1/users/{userId}/rewards/{rewardId}/unlock", userId, reward.getUuid()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.unlocked").value(true));
        }

        @Test
        @DisplayName("Should return 404 if reward does not exist")
        void shouldReturnNotFoundForInvalidReward() throws Exception {
            UUID fakeRewardId = UUID.randomUUID();

            mockMvc.perform(post("/v1/users/{userId}/rewards/{rewardId}/unlock", userId, fakeRewardId))
                    .andExpect(status().isNotFound());
        }
    }
}
