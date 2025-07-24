package com.streaker.integration;

import com.streaker.PostgresTestContainerConfig;
import com.streaker.controller.reward.dto.RewardRequestDto;
import com.streaker.model.Reward;
import com.streaker.model.User;
import com.streaker.repository.RewardRepository;
import com.streaker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser-log", roles = "USER")
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

        User user = new User();
        user.setUsername("testuser-log");
        user.setEmail("testuser-log@example.com");
        user.setPassword("password123");
        user = userRepository.save(user);
        this.userId = user.getUuid();
    }

    @Test
    public void shouldCreateReward() throws Exception {
        RewardRequestDto dto = new RewardRequestDto("Buy Coffee", "Morning treat", 10);

        mockMvc.perform(post("/users/" + userId + "/rewards")
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

        Reward reward1 = new Reward();
        reward1.setName("Book");
        reward1.setPointsRequired(15);
        reward1.setDescription("Hot drink");
        reward1.setUser(user);
        rewardRepository.save(reward1);

        Reward reward2 = new Reward();
        reward2.setName("Coffee");
        reward2.setDescription("Hot drink");
        reward2.setPointsRequired(10);
        reward2.setUser(user);
        rewardRepository.save(reward2);

        mockMvc.perform(get("/users/" + userId + "/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void shouldUnlockReward() throws Exception {
        User user = userRepository.findById(userId).orElseThrow();

        Reward reward = new Reward();
        reward.setName("Tea");
        reward.setPointsRequired(5);
        reward.setDescription("Hot drink");
        reward.setUser(user);
        rewardRepository.save(reward);

        mockMvc.perform(post("/users/" + userId + "/rewards/" + reward.getUuid() + "/unlock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unlocked").value(true));
    }

    @Test
    public void shouldValidateMissingFields() throws Exception {
        User user = userRepository.findById(userId).orElseThrow();
        RewardRequestDto invalidDto = new RewardRequestDto(null, null, 5);

        mockMvc.perform(post("/users/" + userId + "/rewards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}
