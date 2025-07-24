package com.streaker.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.PostgresTestContainerConfig;
import com.streaker.controller.category.dto.CategoryRequestDto;
import com.streaker.model.Category;
import com.streaker.model.User;
import com.streaker.repository.CategoryRepository;
import com.streaker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser-category", roles = "USER")
class CategoryControllerIntegrationTest extends PostgresTestContainerConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID userId;

    @BeforeEach
    void setup() {
        cleanDatabase();

        User user = new User();
        user.setUsername("testuser-category");
        user.setEmail("testuser-category@example.com");
        user.setPassword("password123"); // assuming no encryption here
        user = userRepository.save(user);

        userId = user.getUuid();
    }

    @Test
    void createCategory_shouldPersistAndReturnCategory() throws Exception {
        var dto = new CategoryRequestDto("Fitness", "#FF0000");

        mockMvc.perform(post("/users/{userId}/categories", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Fitness"))
                .andExpect(jsonPath("$.color").value("#FF0000"));

        List<Category> saved = categoryRepository.findAll();
        assertThat(saved).hasSize(1);
        assertThat(saved.getFirst().getName()).isEqualTo("Fitness");
    }

    @Test
    void getCategories_shouldReturnList() throws Exception {
        var category = new Category();
        category.setName("Health");
        category.setColor("#00FF00");
        category.setUser(userRepository.findById(userId).orElseThrow());
        categoryRepository.save(category);

        mockMvc.perform(get("/users/{userId}/categories", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Health"));
    }

    @Test
    void getCategoryById_shouldReturnCategory() throws Exception {
        Category category = new Category();
        category.setName("Work");
        category.setColor("#0000FF");
        category.setUser(userRepository.findById(userId).orElseThrow());
        category = categoryRepository.save(category);

        mockMvc.perform(get("/users/{userId}/categories/{id}", userId, category.getUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(category.getUuid().toString()));
    }

    @Test
    void deleteCategory_shouldRemoveCategory() throws Exception {
        Category category = new Category();
        category.setName("DeleteMe");
        category.setColor("#123456");
        category.setUser(userRepository.findById(userId).orElseThrow());
        category = categoryRepository.save(category);

        mockMvc.perform(delete("/users/{userId}/categories/{id}", userId, category.getUuid()))
                .andExpect(status().isNoContent());

        assertThat(categoryRepository.findById(category.getUuid())).isEmpty();
    }
}

