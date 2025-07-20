package com.streaker.service;

import com.streaker.controller.category.dto.CategoryRequestDto;
import com.streaker.controller.category.dto.CategoryResponseDto;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    CategoryResponseDto createCategory(UUID userId, CategoryRequestDto dto);
    List<CategoryResponseDto> getCategoriesByUser(UUID userId);
    CategoryResponseDto getCategoryById(UUID id);
    void deleteCategory(UUID id);
}
