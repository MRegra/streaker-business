package com.streaker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.controller.user.UserController;
import com.streaker.controller.user.dto.UserDto;
import com.streaker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private UUID userId;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userDto = new UserDto(userId, "john", "john@example.com");
    }

    @Test
    void getAllUsers_shouldReturnList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(userDto));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("john"));
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        when(userService.getUserById(userId)).thenReturn(userDto);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"));
    }

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        when(userService.createUser(any())).thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"));
    }
}
