package com.streaker.integration;

import com.streaker.PostgresTestContainerConfig;
import com.streaker.model.Streak;
import com.streaker.model.User;
import com.streaker.repository.StreakRepository;
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

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
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

        User user = new User();
        user.setUsername("testuser-streak");
        user.setEmail("testuser-streak@example.com");
        user.setPassword("password123");
        user = userRepository.save(user);
        this.userId = user.getUuid();
    }

    @Test
    public void shouldReturnAllStreaksForUser() throws Exception {
        User user = userRepository.findById(userId).orElseThrow();

        Streak streak1 = new Streak();
        streak1.setName("reading-streak");
        streak1.setUser(user);
        streak1.setIsActive(true);
        streak1.setCurrentCount(0);
        streak1.setStartDate(LocalDate.now().minusDays(5));
        streak1.setEndDate(LocalDate.now().minusDays(1));
        streakRepository.save(streak1);

        Streak streak2 = new Streak();
        streak2.setName("reading-streak1");
        streak2.setUser(user);
        streak2.setCurrentCount(0);
        streak2.setIsActive(true);
        streak2.setStartDate(LocalDate.now().minusDays(10));
        streak2.setEndDate(LocalDate.now().minusDays(6));
        streakRepository.save(streak2);

        mockMvc.perform(get("/users/" + userId + "/streaks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void shouldReturnSingleStreakById() throws Exception {
        User user = userRepository.findById(userId).orElseThrow();

        Streak streak = new Streak();
        streak.setUser(user);
        streak.setName("reading-streak2");
        streak.setStartDate(LocalDate.now().minusDays(3));
        streak.setEndDate(LocalDate.now());
        streak.setCurrentCount(0);
        streak.setIsActive(true);
        streak = streakRepository.save(streak);

        mockMvc.perform(get("/users/" + userId + "/streaks/" + streak.getUuid())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(streak.getUuid().toString()));
    }
}