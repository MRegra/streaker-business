package com.streaker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.PostgresTestContainerConfig;
import com.streaker.controller.reward.dto.RewardRequestDto;
import com.streaker.model.Reward;
import com.streaker.model.User;
import com.streaker.repository.RewardRepository;
import com.streaker.repository.UserRepository;
import com.streaker.utils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WithMockUser(username = "testuser-reward", roles = "USER")
public class RewardControllerIntegrationTest extends PostgresTestContainerConfig {

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
    public void setup() {
        cleanDatabase();

        User user = TestDataFactory.createUser("testuser-reward", "testuser-reward@example.com", "password123");
        user = userRepository.save(user);
        this.userId = user.getUuid();
    }

    @Test
    public void shouldCreateReward() throws Exception {
        RewardRequestDto dto = new RewardRequestDto("Buy Coffee", "Morning treat", 10);

        mockMvc.perform(post("/v1/users/" + userId + "/rewards")
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
    public void shouldReturnAllRewardsForUser() throws Exception {
        User user = userRepository.findById(userId).orElseThrow();

        Reward reward1 = TestDataFactory.createReward(user);
        rewardRepository.save(reward1);

        Reward reward2 = TestDataFactory.createReward(user);
        rewardRepository.save(reward2);

        mockMvc.perform(get("/v1/users/" + userId + "/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void shouldUnlockReward() throws Exception {
        User user = userRepository.findById(userId).orElseThrow();
        Reward reward = TestDataFactory.createReward(user);
        reward = rewardRepository.save(reward);

        mockMvc.perform(post("/v1/users/" + userId + "/rewards/" + reward.getUuid() + "/unlock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unlocked").value(true));
    }

    @Test
    public void shouldValidateMissingFields() throws Exception {
        RewardRequestDto invalidDto = new RewardRequestDto(null, null, 5);

        mockMvc.perform(post("/v1/users/" + userId + "/rewards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}