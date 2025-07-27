package com.streaker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.PostgresTestContainerConfig;
import com.streaker.controller.category.dto.CategoryRequestDto;
import com.streaker.integration.utils.IntegrationTestUtils;
import com.streaker.model.Category;
import com.streaker.repository.CategoryRepository;
import com.streaker.repository.UserRepository;
import com.streaker.utils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CategoryControllerIntegrationTest extends PostgresTestContainerConfig {

    private static final String CATEGORY_USER_TEST = "category-user-test";
    private static final String EMAIL = "category@example.com";
    private static final String PASSWORD = "strongpass";
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

        this.jwt = IntegrationTestUtils.registerAndLogin(mockMvc, objectMapper, CATEGORY_USER_TEST, EMAIL, PASSWORD);
        this.userId = userRepository.findByEmail(EMAIL)
                .orElseThrow()
                .getUuid();
    }

    @Test
    void createCategory_shouldSucceed_withValidJwt() throws Exception {
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
    void createCategory_shouldFail_withoutJwt() throws Exception {
        CategoryRequestDto dto = new CategoryRequestDto("Fitness", "#FF0000");

        mockMvc.perform(post("/v1/users/{userId}/categories", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void createCategory_shouldFail_withInvalidDto() throws Exception {
        CategoryRequestDto dto = new CategoryRequestDto("", "");

        mockMvc.perform(post("/v1/users/{userId}/categories", userId)
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCategories_shouldReturnList_forOwner() throws Exception {
        Category category = TestDataFactory.createCategory(userRepository.findById(userId).orElseThrow());
        categoryRepository.save(category);

        mockMvc.perform(get("/v1/users/{userId}/categories", userId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(category.getName()));
    }

    @Test
    void getCategories_shouldFail_withWrongUser() throws Exception {
        String intruderJwt = IntegrationTestUtils.registerAndLogin(mockMvc, objectMapper, "intruder", "bad@evil.com", "password123");

        mockMvc.perform(get("/v1/users/{userId}/categories", userId)
                        .header("Authorization", "Bearer " + intruderJwt))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCategoryById_shouldReturnCategory_forOwner() throws Exception {
        Category category = TestDataFactory.createCategory(userRepository.findById(userId).orElseThrow());
        category = categoryRepository.save(category);

        mockMvc.perform(get("/v1/users/{userId}/categories/{id}", userId, category.getUuid())
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(category.getUuid().toString()));
    }

    @Test
    void getCategoryById_shouldFail_withWrongUser() throws Exception {
        Category category = categoryRepository.save(
                TestDataFactory.createCategory(userRepository.findById(userId).orElseThrow()));

        String intruderJwt = IntegrationTestUtils.registerAndLogin(mockMvc, objectMapper, "intruder", "bad@evil.com", "password123");

        mockMvc.perform(get("/v1/users/{userId}/categories/{id}", userId, category.getUuid())
                        .header("Authorization", "Bearer " + intruderJwt))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteCategory_shouldSucceed_forOwner() throws Exception {
        Category category = TestDataFactory.createCategory(userRepository.findById(userId).orElseThrow());
        category = categoryRepository.save(category);

        mockMvc.perform(delete("/v1/users/{userId}/categories/{id}", userId, category.getUuid())
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isNoContent());

        assertThat(categoryRepository.findById(category.getUuid())).isEmpty();
    }

    @Test
    void deleteCategory_shouldFail_forOtherUser() throws Exception {
        Category category = categoryRepository.save(
                TestDataFactory.createCategory(userRepository.findById(userId).orElseThrow()));

        String intruderJwt = IntegrationTestUtils.registerAndLogin(mockMvc, objectMapper, "intruder", "bad@evil.com", "password123");

        mockMvc.perform(delete("/v1/users/{userId}/categories/{id}", userId, category.getUuid())
                        .header("Authorization", "Bearer " + intruderJwt))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCategoryById_shouldReturn404_ifNotExists() throws Exception {
        UUID fakeId = UUID.randomUUID();

        mockMvc.perform(get("/v1/users/{userId}/categories/{categoryId}", userId, fakeId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isNotFound());
    }
}