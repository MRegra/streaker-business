package com.streaker.service;

import com.streaker.controller.category.dto.CategoryRequestDto;
import com.streaker.controller.category.dto.CategoryResponseDto;
import com.streaker.exception.ResourceNotFoundException;
import com.streaker.model.Category;
import com.streaker.model.User;
import com.streaker.repository.CategoryRepository;
import com.streaker.repository.UserRepository;
import com.streaker.utils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
public class CategoryServiceImplTest {

    private CategoryServiceImpl service;
    private CategoryRepository categoryRepository;
    private UserRepository userRepository;

    private UUID userId;
    private UUID categoryId;
    private User user;
    private Category category;

    @BeforeEach
    void setUp() {
        categoryRepository = mock(CategoryRepository.class);
        userRepository = mock(UserRepository.class);
        service = new CategoryServiceImpl(categoryRepository, userRepository);

        user = TestDataFactory.createUser("jane", "jane@example.com", "password");
        category = TestDataFactory.createCategory(user, "Work", "#000000");

        userId = user.getUuid();
        categoryId = category.getUuid();
    }

    @Test
    void testCreateCategory() {
        CategoryRequestDto dto = new CategoryRequestDto("Work", "#000000");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponseDto result = service.createCategory(userId, dto);

        assertNotNull(result);
        assertEquals("Work", result.name());
        assertEquals("#000000", result.color());
    }

    @Test
    void testGetCategoriesByUser() {
        when(categoryRepository.findByUserUuid(userId)).thenReturn(List.of(category));

        List<CategoryResponseDto> result = service.getCategoriesByUser(userId);

        assertEquals(1, result.size());
        assertEquals("Work", result.getFirst().name());
    }

    @Test
    void testGetCategoryById() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        CategoryResponseDto result = service.getCategoryById(categoryId);

        assertEquals("Work", result.name());
    }

    @Test
    void testDeleteCategory() {
        service.deleteCategory(categoryId);
        verify(categoryRepository).deleteById(categoryId);
    }

    @Test
    void testCreateCategory_UserNotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        CategoryRequestDto dto = new CategoryRequestDto("Work", "#000000");

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                service.createCategory(userId, dto));

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void testGetCategoryById_NotFound_ThrowsException() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                service.getCategoryById(categoryId));

        assertEquals("Category not found", ex.getMessage());
    }
}