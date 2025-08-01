package com.streaker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.BaseIntegrationTest;
import com.streaker.TestContainerConfig;
import com.streaker.config.TestRedisConfig;
import com.streaker.integration.utils.IntegrationTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "rate-limiting.enabled=true")
@Import({TestContainerConfig.class, TestRedisConfig.class})
class RateLimitIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    Environment env;

    @Autowired
    private ObjectMapper objectMapper;

    private final String username = "ratelimit";
    private final String password = "StrongPass123!";

    @BeforeEach
    void setup() throws Exception {
        cleanDatabase();
        String email = "ratelimit@example.com";
        IntegrationTestUtils.registerUser(mockMvc, objectMapper, username, email, password);
    }

    @Test
    void shouldBlockAfterRateLimitIsExceeded() throws Exception {
        for (int i = 1; i <= 5; i++) {
            mockMvc.perform(post("/v1/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of(
                            "username", username,
                            "password", password
                    )))).andExpect(status().isOk());
        }

        // The 6th request should be blocked
        mockMvc.perform(post("/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", password
                        ))))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too many login attempts, please try later"));
    }
}

