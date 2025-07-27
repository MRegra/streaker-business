package com.streaker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.PostgresTestContainerConfig;
import com.streaker.controller.user.dto.CreateUserDto;
import com.streaker.integration.utils.IntegrationTestUtils;
import com.streaker.model.User;
import com.streaker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class UserControllerAuthIntegrationTest extends PostgresTestContainerConfig {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    private String userJwt;
    private UUID userId;

    @BeforeEach
    void setup() throws Exception {
        cleanDatabase();

        this.userJwt = IntegrationTestUtils.registerAndLogin(mockMvc, objectMapper, "jwtUser", "jwt@example.com", "securePass");
        User user = userRepository.findByEmail("jwt@example.com").orElseThrow();
        this.userId = user.getUuid();

    }

    @Test
    void getUserById_withJwt_shouldReturnUser() throws Exception {
        mockMvc.perform(get("/v1/users/{id}", userId)
                        .header("Authorization", "Bearer " + userJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("jwtUser"))
                .andExpect(jsonPath("$.email").value("jwt@example.com"));
    }

    @Test
    void getUserById_withoutJwt_shouldReturn401() throws Exception {
        mockMvc.perform(get("/v1/users/{id}", userId))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void getUserById_withInvalidToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/v1/users/{id}", userId)
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserById_asOtherUser_shouldReturn403() throws Exception {
        // Register second user
        CreateUserDto secondDto = new CreateUserDto("intruder", "badguy@example.com", "hackMe12345");
        mockMvc.perform(post("/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondDto)))
                .andExpect(status().isOk());

        String intruderJwt = IntegrationTestUtils.loginUser(mockMvc, objectMapper, "intruder", "hackMe12345");

        mockMvc.perform(get("/v1/users/{id}", userId)
                        .header("Authorization", "Bearer " + intruderJwt))
                .andExpect(status().isForbidden());
    }
}