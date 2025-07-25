package com.streaker.controller.category;

import com.streaker.controller.category.dto.CategoryRequestDto;
import com.streaker.controller.category.dto.CategoryResponseDto;
import com.streaker.service.CategoryService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/users/{userId}/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "Manage user categories")
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "CategoryService is a Spring-managed bean and safe to inject")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Create a new category")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(
            @PathVariable UUID userId,
            @Valid @RequestBody CategoryRequestDto dto) {
        return ResponseEntity.ok(categoryService.createCategory(userId, dto));
    }

    @Operation(summary = "Get all categories for a user")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getCategories(@PathVariable UUID userId) {
        return ResponseEntity.ok(categoryService.getCategoriesByUser(userId));
    }

    @Operation(summary = "Get a specific category")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getCategory(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @Operation(summary = "Delete a category")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

}
