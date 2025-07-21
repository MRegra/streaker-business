package com.streaker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.config.CustomUserDetailsService;
import com.streaker.config.JwtService;
import com.streaker.controller.streak.dto.StreakDto;
import com.streaker.model.User;
import com.streaker.repository.UserRepository;
import com.streaker.service.StreakService;
import com.streaker.utlis.enums.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class StreakControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StreakService streakService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    private UUID userId, streakId;
    private StreakDto streakDto;
    private String jwtToken;

    @BeforeEach
    void setup() {

        streakId = UUID.randomUUID();
        streakDto = new StreakDto(
                streakId,
                LocalDate.of(2024, 1, 1),
                3,
                10,
                true
        );

        User user = new User();
        user.setUuid(userId);
        user.setUsername("testadmin@example.com");
        user.setEmail("testadmin@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);
        User saved = userRepository.save(user);
        jwtToken = jwtService.generateToken(user);
        userId = saved.getUuid();
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void testGetStreaksByUser() throws Exception {
        Mockito.when(streakService.getStreaksByUser(userId)).thenReturn(List.of(streakDto));

        mockMvc.perform(get("/users/{userId}/streaks", userId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].uuid").value(streakId.toString()));
    }

    @Test
    void testGetStreakById() throws Exception {
        Mockito.when(streakService.getStreak(streakId)).thenReturn(streakDto);

        mockMvc.perform(get("/users/{userId}/streaks/{id}", userId, streakId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(streakId.toString()))
                .andExpect(jsonPath("$.currentCount").value(3))
                .andExpect(jsonPath("$.isActive").value(true));
    }
}
