package com.streaker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.BaseIntegrationTest;
import com.streaker.TestContainerConfig;
import com.streaker.controller.auth.dto.AuthTokensResponse;
import com.streaker.controller.category.dto.CategoryRequestDto;
import com.streaker.integration.utils.IntegrationTestUtils;
import com.streaker.model.Category;
import com.streaker.repository.CategoryRepository;
import com.streaker.repository.UserRepository;
import com.streaker.utils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DisplayName("CategoryController Integration Tests")
@Import(TestContainerConfig.class)
class CategoryControllerIntegrationTest extends BaseIntegrationTest {

    private static final String CATEGORY_USER = "categoryuser";
    private static final String EMAIL = "category@example.com";
    private static final String PASSWORD = "strongPass1";
    private static final String INTRUDER_USER = "intruder";
    private static final String INTRUDER_EMAIL = "bad@evil.com";
    private static final String INTRUDER_PASSWORD = "Password123";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private UserRepository userRepository;

    private UUID userId;
    private String jwt;

    @BeforeEach
    void setup() throws Exception {
        cleanDatabase();
        AuthTokensResponse tokens = IntegrationTestUtils.registerAndLogin(mockMvc, objectMapper, CATEGORY_USER, EMAIL, PASSWORD);
        this.jwt = tokens.accessToken();
        this.userId = userRepository.findByEmail(EMAIL).orElseThrow().getUuid();
    }

    @Nested
    @DisplayName("POST /v1/users/{userId}/categories")
    class CreateCategory {
        @Test
        @DisplayName("should succeed with valid JWT and valid input")
        void shouldCreateCategory() throws Exception {
            CategoryRequestDto dto = new CategoryRequestDto("Fitness", "#FF0000");

            mockMvc.perform(post("/v1/users/{userId}/categories", userId)
                            .header("Authorization", "Bearer " + jwt)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Fitness"))
                    .andExpect(jsonPath("$.color").value("#FF0000"));

            List<Category> saved = categoryRepository.findAll();
            assertThat(saved).hasSize(1);
        }

        @Test
        @DisplayName("should fail without JWT")
        void shouldFailWithoutJwt() throws Exception {
            CategoryRequestDto dto = new CategoryRequestDto("Fitness", "#FF0000");

            mockMvc.perform(post("/v1/users/{userId}/categories", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should fail with invalid DTO")
        void shouldFailWithInvalidDto() throws Exception {
            CategoryRequestDto dto = new CategoryRequestDto("", "");

            mockMvc.perform(post("/v1/users/{userId}/categories", userId)
                            .header("Authorization", "Bearer " + jwt)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 with validation error messages for missing name and color")
        void shouldFailValidationWithBadInput() throws Exception {
            // Invalid payload: missing both required fields
            String invalidJson = """
                    {
                        "name": "",
                        "color": ""
                    }
                    """;

            mockMvc.perform(post("/v1/users/{userId}/categories", userId)
                            .header("Authorization", "Bearer " + jwt)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.message", containsString("color: Color must be")));
        }
    }

    @Nested
    @DisplayName("GET /v1/users/{userId}/categories")
    class GetCategories {
        @Test
        @DisplayName("should return category list for owner")
        void shouldReturnCategories() throws Exception {
            categoryRepository.save(TestDataFactory.createCategory(userRepository.findById(userId).orElseThrow()));

            mockMvc.perform(get("/v1/users/{userId}/categories", userId)
                            .header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").exists());
        }

        @Test
        @DisplayName("should fail with wrong user")
        void shouldFailWithWrongUser() throws Exception {
            AuthTokensResponse intruder = IntegrationTestUtils.registerAndLogin(mockMvc, objectMapper, INTRUDER_USER, INTRUDER_EMAIL, INTRUDER_PASSWORD);

            mockMvc.perform(get("/v1/users/{userId}/categories", userId)
                            .header("Authorization", "Bearer " + intruder.accessToken()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /v1/users/{userId}/categories/{id}")
    class GetCategoryById {
        @Test
        @DisplayName("should return category for owner")
        void shouldReturnCategoryById() throws Exception {
            Category category = categoryRepository.save(TestDataFactory.createCategory(userRepository.findById(userId).orElseThrow()));

            mockMvc.perform(get("/v1/users/{userId}/categories/{id}", userId, category.getUuid())
                            .header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.uuid").value(category.getUuid().toString()));
        }

        @Test
        @DisplayName("should return 404 if category does not exist")
        void shouldReturn404IfNotFound() throws Exception {
            UUID fakeId = UUID.randomUUID();

            mockMvc.perform(get("/v1/users/{userId}/categories/{id}", userId, fakeId)
                            .header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should fail with wrong user")
        void shouldFailWithWrongUser() throws Exception {
            Category category = categoryRepository.save(TestDataFactory.createCategory(userRepository.findById(userId).orElseThrow()));
            AuthTokensResponse intruder = IntegrationTestUtils.registerAndLogin(mockMvc, objectMapper, INTRUDER_USER, INTRUDER_EMAIL, INTRUDER_PASSWORD);

            mockMvc.perform(get("/v1/users/{userId}/categories/{id}", userId, category.getUuid())
                            .header("Authorization", "Bearer " + intruder.accessToken()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /v1/users/{userId}/categories/{id}")
    class DeleteCategory {
        @Test
        @DisplayName("should succeed for owner")
        void shouldDeleteCategory() throws Exception {
            Category category = categoryRepository.save(TestDataFactory.createCategory(userRepository.findById(userId).orElseThrow()));

            mockMvc.perform(delete("/v1/users/{userId}/categories/{id}", userId, category.getUuid())
                            .header("Authorization", "Bearer " + jwt))
                    .andExpect(status().isNoContent());

            assertThat(categoryRepository.findById(category.getUuid())).isEmpty();
        }

        @Test
        @DisplayName("should fail for other user")
        void shouldFailForOtherUser() throws Exception {
            Category category = categoryRepository.save(TestDataFactory.createCategory(userRepository.findById(userId).orElseThrow()));
            AuthTokensResponse intruder = IntegrationTestUtils.registerAndLogin(mockMvc, objectMapper, INTRUDER_USER, INTRUDER_EMAIL, INTRUDER_PASSWORD);

            mockMvc.perform(delete("/v1/users/{userId}/categories/{id}", userId, category.getUuid())
                            .header("Authorization", "Bearer " + intruder.accessToken()))
                    .andExpect(status().isForbidden());
        }
    }
}
