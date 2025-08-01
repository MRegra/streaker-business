package com.streaker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.BaseIntegrationTest;
import com.streaker.TestContainerConfig;
import com.streaker.controller.auth.dto.AuthTokensResponse;
import com.streaker.controller.reward.dto.RewardRequestDto;
import com.streaker.integration.utils.IntegrationTestUtils;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DisplayName("RewardController Integration Tests")
@Import(TestContainerConfig.class)
class RewardControllerIntegrationTest extends BaseIntegrationTest {

    public static final String TESTUSER_REWARD = "testuser_reward";
    public static final String EMAIL = "testuser-reward@example.com";
    public static final String PASSWORD = "Password123";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RewardRepository rewardRepository;
    @Autowired
    private UserRepository userRepository;

    private UUID userId;
    private String jwt;

    @BeforeEach
    void setUp() throws Exception {
        cleanDatabase();

        AuthTokensResponse tokens = IntegrationTestUtils.registerAndLogin(mockMvc, objectMapper,
                TESTUSER_REWARD, EMAIL, PASSWORD);

        jwt = tokens.accessToken();
        userId = userRepository.findByEmail(EMAIL).orElseThrow().getUuid();
    }

    @Nested
    @DisplayName("POST /v1/users/{userId}/rewards")
    class CreateReward {

        @Test
        @DisplayName("Should create a reward and return it")
        void shouldCreateRewardSuccessfully() throws Exception {
            RewardRequestDto dto = new RewardRequestDto("Buy Coffee", "Morning treat", 10);

            mockMvc.perform(post("/v1/users/{userId}/rewards", userId)
                            .header("Authorization", "Bearer " + jwt)
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
            String invalidJson = """
                    {
                        "name": null,
                        "description": null,
                        "pointsRequired": 5
                    }
                    """;

            mockMvc.perform(post("/v1/users/{userId}/rewards", userId)
                            .header("Authorization", "Bearer " + jwt)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.message", containsString("name: Name must be specified")));
        }

        @Test
        @DisplayName("Should return 400 for invalid input values")
        void shouldReturn400WhenRewardRequestIsInvalid() throws Exception {
            String invalidJson = """
                    {
                        "name": "",
                        "description": "This description is way too long %s",
                        "pointsRequired": 0
                    }
                    """.formatted("a".repeat(201));

            mockMvc.perform(post("/v1/users/{userId}/rewards", userId)
                            .header("Authorization", "Bearer " + jwt)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.message", containsString("name: Name must be specified")))
                    .andExpect(jsonPath("$.message", containsString("description: Description's maximum length is 200 chars.")))
                    .andExpect(jsonPath("$.message", containsString("pointsRequired: Points required must be greater than zero")));
        }

        @Test
        @DisplayName("Should return 403 when user does not exist - Forbidden")
        void shouldReturnNotFoundForNonExistentUser() throws Exception {
            RewardRequestDto dto = new RewardRequestDto("Gym Pass", "Weekend pass", 15);
            UUID fakeUserId = UUID.randomUUID();

            mockMvc.perform(post("/v1/users/{userId}/rewards", fakeUserId)
                            .header("Authorization", "Bearer " + jwt)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isForbidden());
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

            mockMvc.perform(get("/v1/users/{userId}/rewards", userId)
                            .header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("Should return empty list if user has no rewards")
        void shouldReturnEmptyRewardList() throws Exception {
            mockMvc.perform(get("/v1/users/{userId}/rewards", userId)
                            .header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Should return 403 if user does not exist - Forbidden")
        void shouldReturnNotFoundForNonExistentUser() throws Exception {
            UUID fakeUserId = UUID.randomUUID();

            mockMvc.perform(get("/v1/users/{userId}/rewards", fakeUserId)
                            .header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isForbidden());
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

            mockMvc.perform(post("/v1/users/{userId}/rewards/{rewardId}/unlock", userId, reward.getUuid())
                            .header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.unlocked").value(true));
        }

        @Test
        @DisplayName("Should return 404 if reward does not exist")
        void shouldReturnNotFoundForInvalidReward() throws Exception {
            UUID fakeRewardId = UUID.randomUUID();

            mockMvc.perform(post("/v1/users/{userId}/rewards/{rewardId}/unlock", userId, fakeRewardId)
                            .header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isNotFound());
        }
    }
}
