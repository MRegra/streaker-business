package com.streaker.service;

import com.streaker.controller.category.dto.CategoryRequestDto;
import com.streaker.controller.category.dto.CategoryResponseDto;
import com.streaker.exception.ResourceNotFoundException;
import com.streaker.model.Category;
import com.streaker.model.User;
import com.streaker.repository.CategoryRepository;
import com.streaker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        user = new User();
        user.setUuid(userId);

        category = new Category();
        category.setUuid(categoryId);
        category.setName("Work");
        category.setColor("#000000");
        category.setUser(user);
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

        String message = assertThrows(ResourceNotFoundException.class, () -> service.createCategory(userId, dto)).getMessage();
        assertEquals("User not found", message, "User does not exist in the system");
    }

    @Test
    void testGetCategoryById_NotFound_ThrowsException() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        String message = assertThrows(ResourceNotFoundException.class, () -> service.getCategoryById(categoryId)).getMessage();
        assertEquals("Category not found", message, "Category does not exist in the system");

    }
}
