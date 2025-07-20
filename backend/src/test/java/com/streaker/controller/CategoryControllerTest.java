package com.streaker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaker.controller.category.CategoryController;
import com.streaker.controller.category.dto.CategoryRequestDto;
import com.streaker.controller.category.dto.CategoryResponseDto;
import com.streaker.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public @WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private UUID categoryId;
    private CategoryRequestDto sampleCategory;
    private CategoryResponseDto sampleCategoryResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        sampleCategory = new CategoryRequestDto("Fitness", "#FF0000");
        sampleCategoryResponse = new CategoryResponseDto(categoryId, "Fitness", "#FF0000");
    }

    @Test
    void testCreateCategory() throws Exception {
        Mockito.when(categoryService.createCategory(eq(userId), any(CategoryRequestDto.class)))
                .thenReturn(sampleCategoryResponse);

        mockMvc.perform(post("/users/{userId}/categories", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("Fitness"))
                .andExpect(jsonPath("$.color").value("#FF0000"));
    }

    @Test
    void testGetCategories() throws Exception {
        Mockito.when(categoryService.getCategoriesByUser(userId))
                .thenReturn(List.of(sampleCategoryResponse));

        mockMvc.perform(get("/users/{userId}/categories", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Fitness"));
    }

    @Test
    void testGetCategory() throws Exception {
        Mockito.when(categoryService.getCategoryById(categoryId))
                .thenReturn(sampleCategoryResponse);

        mockMvc.perform(get("/users/{userId}/categories/{id}", userId, categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(categoryId.toString()));
    }

    @Test
    void testDeleteCategory() throws Exception {
        mockMvc.perform(delete("/users/{userId}/categories/{id}", userId, categoryId))
                .andExpect(status().isNoContent());

        Mockito.verify(categoryService).deleteCategory(categoryId);
    }
}
