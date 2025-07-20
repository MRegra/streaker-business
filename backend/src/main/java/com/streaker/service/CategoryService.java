package com.streaker.service;

import com.streaker.controller.category.dto.CategoryDto;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    CategoryDto createCategory(UUID userId, CategoryDto dto);
    List<CategoryDto> getCategoriesByUser(UUID userId);
    CategoryDto getCategoryById(UUID id);
    void deleteCategory(UUID id);
}
