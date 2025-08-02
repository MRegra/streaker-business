package com.streaker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.BaseIntegrationTest;
import com.streaker.TestContainerConfig;
import com.streaker.controller.user.dto.LoginUserDto;
import com.streaker.integration.utils.IntegrationTestUtils;
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
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Security Headers Integration Tests")
@Import(TestContainerConfig.class)
class SecurityHeadersIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws Exception {
        cleanDatabase();
        IntegrationTestUtils.registerUser(mockMvc, objectMapper, USER_TEST_USERNAME, USER_TEST_EMAIL, PASSWORD);
    }

    @Test
    @DisplayName("POST /v1/users/login - should include core security headers")
    void loginResponse_shouldIncludeSecurityHeaders() throws Exception {
        LoginUserDto loginDto = new LoginUserDto(USER_TEST_USERNAME, PASSWORD);

        mockMvc.perform(post("/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", equalTo("nosniff")))
                .andExpect(header().string("X-Frame-Options", equalTo("DENY")))
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'self'")));
    }

    @Test
    @DisplayName("GET /actuator/health - should include headers on non-auth requests")
    void healthCheck_shouldIncludeSecurityHeaders() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", equalTo("nosniff")))
                .andExpect(header().string("X-Frame-Options", equalTo("DENY")))
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'self'")));
    }
}
