package com.ceylabs.fintrackerbackend.controller;

import com.ceylabs.fintrackerbackend.dto.CategoryCreateRequest;
import com.ceylabs.fintrackerbackend.dto.CategoryResponse;
import com.ceylabs.fintrackerbackend.enums.CategoryType;
import com.ceylabs.fintrackerbackend.model.Category;
import com.ceylabs.fintrackerbackend.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // Create a new category
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get all accessible categories by user (system defaults + user's own)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CategoryResponse>> getAccessibleCategoriesByUser(@PathVariable Long userId) {
        List<CategoryResponse> categories = categoryService.getAccessibleCategoriesByUser(userId);
        return ResponseEntity.ok(categories);
    }

    // Get a specific category by ID
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        Optional<Category> category = categoryService.getCategoryById(id);
        return category.map(categoryService::mapToResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update a category
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryCreateRequest request) {
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(response);
    }

    // Delete a category
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // Get categories by user and type
    @GetMapping("/user/{userId}/type/{categoryType}")
    public ResponseEntity<List<CategoryResponse>> getCategoriesByUserAndType(
            @PathVariable Long userId,
            @PathVariable CategoryType categoryType) {
        List<CategoryResponse> categories = categoryService.getAccessibleCategoriesByUserAndType(userId, categoryType);
        return ResponseEntity.ok(categories);
    }

    // Get expense categories by user
    @GetMapping("/user/{userId}/expense")
    public ResponseEntity<List<CategoryResponse>> getExpenseCategories(@PathVariable Long userId) {
        List<CategoryResponse> categories = categoryService.getExpenseCategoriesByUser(userId);
        return ResponseEntity.ok(categories);
    }

    // Get income categories by user
    @GetMapping("/user/{userId}/income")
    public ResponseEntity<List<CategoryResponse>> getIncomeCategories(@PathVariable Long userId) {
        List<CategoryResponse> categories = categoryService.getIncomeCategoriesByUser(userId);
        return ResponseEntity.ok(categories);
    }

    // Get user-created categories only (excluding system defaults)
    @GetMapping("/user/{userId}/custom")
    public ResponseEntity<List<CategoryResponse>> getUserCreatedCategories(@PathVariable Long userId) {
        List<CategoryResponse> categories = categoryService.getUserCreatedCategories(userId);
        return ResponseEntity.ok(categories);
    }

    // Get system default categories
    @GetMapping("/system-defaults")
    public ResponseEntity<List<CategoryResponse>> getSystemDefaultCategories() {
        List<CategoryResponse> categories = categoryService.getSystemDefaultCategories();
        return ResponseEntity.ok(categories);
    }

    // Search categories by name
    @GetMapping("/user/{userId}/search")
    public ResponseEntity<List<CategoryResponse>> searchCategories(
            @PathVariable Long userId,
            @RequestParam String query) {
        List<CategoryResponse> categories = categoryService.searchCategoriesByName(userId, query);
        return ResponseEntity.ok(categories);
    }
}
