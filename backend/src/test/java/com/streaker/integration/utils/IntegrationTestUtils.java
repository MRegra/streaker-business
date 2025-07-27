package com.streaker.integration.utils;

import com.streaker.controller.user.dto.CreateUserDto;
import com.streaker.controller.user.dto.LoginUserDto;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class IntegrationTestUtils {

    public static void registerUser(MockMvc mockMvc, ObjectMapper mapper, String username, String email, String password) throws Exception {
        CreateUserDto registerDto = new CreateUserDto(username, email, password);

        mockMvc.perform(post("/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(registerDto)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 200 && status != 201 && status != 409) {
                        throw new IllegalStateException("Failed to register user: " + status);
                    }
                });
    }

    public static String loginUser(MockMvc mockMvc, ObjectMapper mapper, String username, String password) throws Exception {
        LoginUserDto loginDto = new LoginUserDto(username, password);

        MvcResult result = mockMvc.perform(post("/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginDto)))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return mapper.readTree(response).get("token").asText();
    }

    public static String registerAndLogin(MockMvc mockMvc, ObjectMapper mapper, String username, String email, String password) throws Exception {
        registerUser(mockMvc, mapper, username, email, password);
        return loginUser(mockMvc, mapper, username, password);
    }
}