package com.streaker.controller.category;

import com.streaker.config.JwtAuthorizationValidator;
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
import org.springframework.web.bind.annotation.RequestHeader;
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

    private final JwtAuthorizationValidator jwtAuthorizationValidator;

    private final CategoryService categoryService;

    @Operation(summary = "Create a new category")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID userId,
            @Valid @RequestBody CategoryRequestDto dto) {
        jwtAuthorizationValidator.validateToken(authHeader, userId);
        return ResponseEntity.ok(categoryService.createCategory(userId, dto));
    }

    @Operation(summary = "Get all categories for a user")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getCategories(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID userId
    ) {
        jwtAuthorizationValidator.validateToken(authHeader, userId);
        return ResponseEntity.ok(categoryService.getCategoriesByUser(userId));
    }

    @Operation(summary = "Get a specific category")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDto> getCategory(
            @PathVariable UUID userId,
            @PathVariable UUID categoryId,
            @RequestHeader("Authorization") String authHeader
    ) {
        jwtAuthorizationValidator.validateToken(authHeader, userId);
        return ResponseEntity.ok(categoryService.getCategoryByUserUuidAndCategoryId(userId, categoryId));
    }

    @Operation(summary = "Delete a category")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable UUID userId,
            @PathVariable UUID categoryId,
            @RequestHeader("Authorization") String authHeader) {
        jwtAuthorizationValidator.validateToken(authHeader, userId);
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

}
