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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@DisplayName("CategoryServiceImpl Tests")
class CategoryServiceImplTest {

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

    @Nested
    @DisplayName("createCategory()")
    class CreateCategory {

        @Test
        @DisplayName("should create category for valid user")
        void shouldCreateCategory() {
            CategoryRequestDto dto = new CategoryRequestDto("Work", "#000000");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(categoryRepository.save(any(Category.class))).thenReturn(category);

            CategoryResponseDto result = service.createCategory(userId, dto);

            assertAll(
                    () -> assertNotNull(result),
                    () -> assertEquals("Work", result.name()),
                    () -> assertEquals("#000000", result.color())
            );
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when user does not exist")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());
            CategoryRequestDto dto = new CategoryRequestDto("Work", "#000000");

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> service.createCategory(userId, dto));

            assertEquals("User not found", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("getCategoriesByUser()")
    class GetCategoriesByUser {

        @Test
        @DisplayName("should return list of categories for user")
        void shouldReturnCategories() {
            when(categoryRepository.findByUserUuid(userId)).thenReturn(List.of(category));

            List<CategoryResponseDto> result = service.getCategoriesByUser(userId);

            assertAll(
                    () -> assertEquals(1, result.size()),
                    () -> assertEquals("Work", result.getFirst().name())
            );
        }

        @Test
        @DisplayName("should return empty list when user has no categories")
        void shouldReturnEmptyList() {
            when(categoryRepository.findByUserUuid(userId)).thenReturn(List.of());

            List<CategoryResponseDto> result = service.getCategoriesByUser(userId);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getCategoryById()")
    class GetCategoryById {

        @Test
        @DisplayName("should return category by id")
        void shouldReturnCategory() {
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

            CategoryResponseDto result = service.getCategoryById(categoryId);

            assertEquals("Work", result.name());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when category not found")
        void shouldThrowIfNotFound() {
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> service.getCategoryById(categoryId));

            assertEquals("Category not found", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("deleteCategory()")
    class DeleteCategory {

        @Test
        @DisplayName("should delete category by id")
        void shouldDeleteCategory() {
            service.deleteCategory(categoryId);
            verify(categoryRepository).deleteById(categoryId);
        }
    }
}
