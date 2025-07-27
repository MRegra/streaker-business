package com.streaker.service;

import com.streaker.controller.category.dto.CategoryRequestDto;
import com.streaker.controller.category.dto.CategoryResponseDto;
import com.streaker.exception.ResourceNotFoundException;
import com.streaker.model.Category;
import com.streaker.model.User;
import com.streaker.repository.CategoryRepository;
import com.streaker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    public CategoryResponseDto createCategory(UUID userId, CategoryRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = new Category();
        category.setName(dto.name());
        category.setColor(dto.color());
        category.setUser(user);

        return mapToDto(categoryRepository.save(category));
    }

    @Override
    public List<CategoryResponseDto> getCategoriesByUser(UUID userId) {
        return categoryRepository.findByUserUuid(userId).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public CategoryResponseDto getCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    @Override
    public CategoryResponseDto getCategoryByUserUuidAndCategoryId(UUID userUuid, UUID categoryUuid) {
        return categoryRepository.findByUserUuidAndUuid(userUuid, categoryUuid)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    @Override
    public void deleteCategory(UUID id) {
        categoryRepository.deleteById(id);
    }

    private CategoryResponseDto mapToDto(Category category) {
        return new CategoryResponseDto(
                category.getUuid(),
                category.getName(),
                category.getColor()
        );
    }
}
