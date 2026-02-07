package com.ceylabs.fintrackerbackend.dto;

import com.ceylabs.fintrackerbackend.enums.CategoryType;
import jakarta.validation.constraints.*;

public class CategoryCreateRequest {

//    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Category name is required")
    @Size(min = 1, max = 255, message = "Category name must be between 1 and 255 characters")
    private String name;

    @NotNull(message = "Category type is required")
    private CategoryType categoryType;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex code")
    private String color = "#000000";

    @Size(max = 50, message = "Icon name must not exceed 50 characters")
    private String icon;

    // System default flag is typically set by admin/system, not by user
    // So we don't include it in create request for regular users

    // Constructors
    public CategoryCreateRequest() {}

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryType getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(CategoryType categoryType) {
        this.categoryType = categoryType;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
