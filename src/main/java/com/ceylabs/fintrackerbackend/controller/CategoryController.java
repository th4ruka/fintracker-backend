package com.ceylabs.fintrackerbackend.controller;

import com.ceylabs.fintrackerbackend.dto.CategoryCreateRequest;
import com.ceylabs.fintrackerbackend.dto.CategoryResponse;
import com.ceylabs.fintrackerbackend.enums.CategoryType;
import com.ceylabs.fintrackerbackend.model.Category;
import com.ceylabs.fintrackerbackend.security.CustomUserDetails;
import com.ceylabs.fintrackerbackend.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * Create a new category for the authenticated user
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CategoryCreateRequest request) {
        // Override userId from request with authenticated user's ID for security
        request.setUserId(userDetails.getId());
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all accessible categories for the authenticated user (system defaults + user's own)
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAccessibleCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<CategoryResponse> categories = categoryService.getAccessibleCategoriesByUser(userDetails.getId());
        return ResponseEntity.ok(categories);
    }

    /**
     * Get a specific category by ID (with ownership verification)
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        Optional<Category> category = categoryService.getCategoryById(id);

        if (category.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verify ownership: category must be system default (user is null) or belong to authenticated user
        if (category.get().getUser() != null &&
            !category.get().getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(categoryService.mapToResponse(category.get()));
    }

    /**
     * Update a category (with ownership verification)
     * Users can only update their own categories, not system defaults
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody CategoryCreateRequest request) {
        Optional<Category> category = categoryService.getCategoryById(id);

        if (category.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verify ownership: can only update user's own categories (not system defaults)
        if (category.get().getUser() == null ||
            !category.get().getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a category (with ownership verification)
     * Users can only delete their own categories, not system defaults
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        Optional<Category> category = categoryService.getCategoryById(id);

        if (category.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Verify ownership: can only delete user's own categories (not system defaults)
        if (category.get().getUser() == null ||
            !category.get().getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get categories by type for the authenticated user
     */
    @GetMapping("/type/{categoryType}")
    public ResponseEntity<List<CategoryResponse>> getCategoriesByType(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable CategoryType categoryType) {
        List<CategoryResponse> categories = categoryService.getAccessibleCategoriesByUserAndType(
                userDetails.getId(), categoryType);
        return ResponseEntity.ok(categories);
    }

    /**
     * Get expense categories for the authenticated user
     */
    @GetMapping("/expense")
    public ResponseEntity<List<CategoryResponse>> getExpenseCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<CategoryResponse> categories = categoryService.getExpenseCategoriesByUser(userDetails.getId());
        return ResponseEntity.ok(categories);
    }

    /**
     * Get income categories for the authenticated user
     */
    @GetMapping("/income")
    public ResponseEntity<List<CategoryResponse>> getIncomeCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<CategoryResponse> categories = categoryService.getIncomeCategoriesByUser(userDetails.getId());
        return ResponseEntity.ok(categories);
    }

    /**
     * Get user-created categories only (excluding system defaults) for the authenticated user
     */
    @GetMapping("/custom")
    public ResponseEntity<List<CategoryResponse>> getUserCreatedCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<CategoryResponse> categories = categoryService.getUserCreatedCategories(userDetails.getId());
        return ResponseEntity.ok(categories);
    }

    // Get system default categories
    @GetMapping("/system-defaults")
    public ResponseEntity<List<CategoryResponse>> getSystemDefaultCategories() {
        List<CategoryResponse> categories = categoryService.getSystemDefaultCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Search categories by name for the authenticated user
     */
    @GetMapping("/search")
    public ResponseEntity<List<CategoryResponse>> searchCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String query) {
        List<CategoryResponse> categories = categoryService.searchCategoriesByName(userDetails.getId(), query);
        return ResponseEntity.ok(categories);
    }
}
