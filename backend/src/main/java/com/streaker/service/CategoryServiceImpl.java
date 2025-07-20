package com.streaker.service;

import com.streaker.controller.category.dto.CategoryDto;
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
    public CategoryDto createCategory(UUID userId, CategoryDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Category category = new Category();
        category.setName(dto.getName());
        category.setColor(dto.getColor());
        category.setUser(user);

        return mapToDto(categoryRepository.save(category));
    }

    @Override
    public List<CategoryDto> getCategoriesByUser(UUID userId) {
        return categoryRepository.findByUserUuid(userId).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public CategoryDto getCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Override
    public void deleteCategory(UUID id) {
        categoryRepository.deleteById(id);
    }

    private CategoryDto mapToDto(Category category) {
        return new CategoryDto(
                category.getUuid(),
                category.getName(),
                category.getColor()
        );
    }
}
