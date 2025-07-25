package com.streaker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.PostgresTestContainerConfig;
import com.streaker.controller.user.dto.CreateUserDto;
import com.streaker.model.User;
import com.streaker.repository.CategoryRepository;
import com.streaker.repository.HabitRepository;
import com.streaker.repository.StreakRepository;
import com.streaker.repository.UserRepository;
import org.assertj.core.api.AssertionsForClassTypes;
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
@WithMockUser(username = "testuser-user", roles = "USER")
public class UserControllerIntegrationTest extends PostgresTestContainerConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private StreakRepository streakRepository;

    @BeforeEach
    void setup() {
        cleanDatabase();
    }

    @Test
    void createUser_shouldPersistAndReturnUser() throws Exception {
        CreateUserDto userDto = new CreateUserDto("john", "john@example.com", "securePassword");

        mockMvc.perform(post("/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(1);
        AssertionsForClassTypes.assertThat(users.getFirst().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        User user = new User();
        user.setUsername("jane");
        user.setEmail("jane@example.com");
        user.setPassword("encryptedPassword");
        user = userRepository.save(user);

        mockMvc.perform(get("/v1/users/{id}", user.getUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("jane"))
                .andExpect(jsonPath("$.email").value("jane@example.com"));
    }

    @Test
    void getUserById_shouldReturn404ForMissing() throws Exception {
        mockMvc.perform(get("/v1/users/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

}
