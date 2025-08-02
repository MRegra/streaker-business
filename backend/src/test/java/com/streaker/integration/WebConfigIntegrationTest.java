package com.streaker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.BaseIntegrationTest;
import com.streaker.TestContainerConfig;
import com.streaker.controller.user.dto.LoginUserDto;
import com.streaker.integration.utils.IntegrationTestUtils;
import com.streaker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("WebConfig [Cors] Integration Tests")
@Import(TestContainerConfig.class)
public class WebConfigIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() throws Exception {
        cleanDatabase();
        IntegrationTestUtils.registerUser(mockMvc, objectMapper, USER_TEST_USERNAME, USER_TEST_EMAIL, PASSWORD);
    }

    @Test
    void requestFromTrustedOrigin_shouldPassCorsCheck() throws Exception {
        LoginUserDto loginDto = new LoginUserDto(USER_TEST_USERNAME, PASSWORD);

        mockMvc.perform(post("/v1/users/login")
                        .header("Origin", "https://app.streaker.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://app.streaker.com"));
    }

    @Test
    void requestFromUntrustedOrigin_shouldFailCorsCheck() throws Exception {
        LoginUserDto loginDto = new LoginUserDto(USER_TEST_USERNAME, PASSWORD);
        mockMvc.perform(post("/v1/users/login")
                        .header("Origin", "https://evil.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Invalid CORS request")));
    }
}
