package com.streaker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.PostgresTestContainerConfig;
import com.streaker.model.Streak;
import com.streaker.model.User;
import com.streaker.repository.StreakRepository;
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

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WithMockUser(username = "testuser-streak", roles = "USER")
public class StreakControllerIntegrationTest extends PostgresTestContainerConfig {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StreakRepository streakRepository;

    private UUID userId;

    @BeforeEach
    public void setup() {
        cleanDatabase();

        User user = TestDataFactory.createUser("testuser-streak", "testuser-streak@example.com", "password123");
        user = userRepository.save(user);
        this.userId = user.getUuid();
    }

    @Test
    public void shouldReturnAllStreaksForUser() throws Exception {
        User user = userRepository.findById(userId).orElseThrow();

        Streak streak1 = TestDataFactory.createStreak(user);
        streakRepository.save(streak1);

        Streak streak2 = TestDataFactory.createStreak(user);
        streakRepository.save(streak2);

        mockMvc.perform(get("/v1/users/" + userId + "/streaks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void shouldReturnSingleStreakById() throws Exception {
        User user = userRepository.findById(userId).orElseThrow();

        Streak streak = TestDataFactory.createStreak(user);
        streak = streakRepository.save(streak);

        mockMvc.perform(get("/v1/users/" + userId + "/streaks/" + streak.getUuid())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(streak.getUuid().toString()));
    }
}