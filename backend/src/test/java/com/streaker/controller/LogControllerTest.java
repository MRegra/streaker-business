package com.streaker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.config.CustomUserDetailsService;
import com.streaker.config.JwtService;
import com.streaker.controller.log.dto.LogRequestDto;
import com.streaker.controller.log.dto.LogResponseDto;
import com.streaker.model.User;
import com.streaker.repository.UserRepository;
import com.streaker.service.LogService;
import com.streaker.utlis.enums.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class LogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LogService logService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    private UUID habitId, logId;
    private LogRequestDto requestDto;
    private LogResponseDto responseDto;
    private String jwtToken;

    @BeforeEach
    void setup() {
        habitId = UUID.randomUUID();
        logId = UUID.randomUUID();

        requestDto = new LogRequestDto(LocalDate.of(2025, 7, 20), true);
        responseDto = new LogResponseDto(logId, requestDto.date(), true, habitId);


        User user = new User();
        user.setUsername("testadmin@example.com");
        user.setEmail("testadmin@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);
        userRepository.save(user);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "testadmin@example.com",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        jwtToken = jwtService.generateToken(userDetails);
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void testCreateLog() throws Exception {
        Mockito.when(logService.createLog(any(), any())).thenReturn(responseDto);

        mockMvc.perform(post("/users/habits/{habitId}/logs", habitId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(logId.toString()))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void testGetLogsByHabit() throws Exception {
        Mockito.when(logService.getLogsByHabit(habitId)).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/users/habits/{habitId}/logs", habitId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].uuid").value(logId.toString()));
    }

    @Test
    void testGetLog() throws Exception {
        Mockito.when(logService.getLog(logId)).thenReturn(responseDto);

        mockMvc.perform(get("/users/habits/{habitId}/logs/{logId}", habitId, logId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(logId.toString()));
    }

    @Test
    void testMarkLogCompleted() throws Exception {
        Mockito.when(logService.markLogCompleted(logId)).thenReturn(responseDto);

        mockMvc.perform(post("/users/habits/{habitId}/logs/{logId}/complete", habitId, logId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
    }

}
