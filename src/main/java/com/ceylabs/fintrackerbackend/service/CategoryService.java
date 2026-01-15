package com.ceylabs.fintrackerbackend.service;

import com.ceylabs.fintrackerbackend.dto.CategoryCreateRequest;
import com.ceylabs.fintrackerbackend.dto.CategoryResponse;
import com.ceylabs.fintrackerbackend.enums.CategoryType;
import com.ceylabs.fintrackerbackend.model.Category;
import com.ceylabs.fintrackerbackend.model.User;
import com.ceylabs.fintrackerbackend.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserService userService;

    // Create a new category
    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        // Validate user exists
        User user = userService.getUserById(request.getUserId())
                .orElseThrow(() -> new IllegalStateException("User with ID " + request.getUserId() + " does not exist"));

        // Check for duplicate category name for this user
        if (categoryRepository.existsByNameAndUserId(request.getName(), request.getUserId())) {
            throw new IllegalStateException("Category with name '" + request.getName() + "' already exists for this user");
        }

        // Create category entity
        Category category = new Category();
        category.setUser(user);
        category.setName(request.getName());
        category.setCategoryType(request.getCategoryType());
        category.setColor(request.getColor());
        category.setIcon(request.getIcon());
        category.setIsSystemDefault(false); // User-created categories are not system defaults

        // Save category
        Category savedCategory = categoryRepository.save(category);

        return mapToResponse(savedCategory);
    }

    // Update an existing category
    @Transactional
    public CategoryResponse updateCategory(Long categoryId, CategoryCreateRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalStateException("Category with ID " + categoryId + " does not exist"));

        // Prevent updating system default categories
        if (category.getIsSystemDefault()) {
            throw new IllegalStateException("Cannot update system default categories");
        }

        // Validate category belongs to user
        if (!category.getUser().getId().equals(request.getUserId())) {
            throw new IllegalStateException("Category does not belong to the specified user");
        }

        // Check for duplicate name (excluding current category)
        Optional<Category> existingCategory = categoryRepository.findByNameAndUserId(request.getName(), request.getUserId());
        if (existingCategory.isPresent() && !existingCategory.get().getId().equals(categoryId)) {
            throw new IllegalStateException("Category with name '" + request.getName() + "' already exists for this user");
        }

        // Update fields
        category.setName(request.getName());
        category.setCategoryType(request.getCategoryType());
        category.setColor(request.getColor());
        category.setIcon(request.getIcon());

        // Save updated category
        Category updatedCategory = categoryRepository.save(category);

        return mapToResponse(updatedCategory);
    }

    // Delete a category
    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalStateException("Category with ID " + categoryId + " does not exist"));

        // Prevent deleting system default categories
        if (category.getIsSystemDefault()) {
            throw new IllegalStateException("Cannot delete system default categories");
        }

        // Note: Related financial records will have their category set to null
        // This is handled by the database (if ON DELETE SET NULL is configured)
        // Or you can manually update records here

        categoryRepository.delete(category);
    }

    // Get category by ID
    public Optional<Category> getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId);
    }

    // Get all categories by user (including system defaults)
    public List<CategoryResponse> getAccessibleCategoriesByUser(Long userId) {
        return categoryRepository.findAccessibleCategoriesByUser(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get categories by user and type
    public List<CategoryResponse> getAccessibleCategoriesByUserAndType(Long userId, CategoryType categoryType) {
        return categoryRepository.findAccessibleCategoriesByUserAndType(userId, categoryType).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get expense categories by user
    public List<CategoryResponse> getExpenseCategoriesByUser(Long userId) {
        return categoryRepository.findExpenseCategoriesByUser(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get income categories by user
    public List<CategoryResponse> getIncomeCategoriesByUser(Long userId) {
        return categoryRepository.findIncomeCategoriesByUser(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get user-created categories only
    public List<CategoryResponse> getUserCreatedCategories(Long userId) {
        return categoryRepository.findByUserIdAndIsSystemDefaultFalse(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get system default categories
    public List<CategoryResponse> getSystemDefaultCategories() {
        return categoryRepository.findByIsSystemDefaultTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Search categories by name
    public List<CategoryResponse> searchCategoriesByName(Long userId, String searchTerm) {
        return categoryRepository.findByUserIdAndNameContainingIgnoreCase(userId, searchTerm).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Helper method: Map entity to response DTO
    public CategoryResponse mapToResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setUserId(category.getUser().getId());
        response.setName(category.getName());
        response.setCategoryType(category.getCategoryType());
        response.setColor(category.getColor());
        response.setIcon(category.getIcon());
        response.setCreatedDate(category.getCreatedDate());
        response.setIsSystemDefault(category.getIsSystemDefault());
        return response;
    }
}
