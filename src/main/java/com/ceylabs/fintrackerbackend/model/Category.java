package com.ceylabs.fintrackerbackend.model;

import com.ceylabs.fintrackerbackend.enums.CategoryType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Category name is required")
    @Size(min = 1, max = 255)
    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private CategoryType categoryType = CategoryType.EXPENSE;

    @Column(length = 7, nullable = false)
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be valid hex code")
    private String color = "#000000";

    @Size(max = 50, message = "Icon name must not exceed 50 characters")
    @Column(length = 50)
    private String icon; // Icon name or emoji for the category

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate;

    @Column(name = "is_system_default", nullable = false)
    private Boolean isSystemDefault = false; // System-provided categories vs user-created

    // Constructors
    public Category() {}

    public Category(String name, CategoryType categoryType, User user) {
        this.name = name;
        this.categoryType = categoryType;
        this.user = user;
        this.createdDate = LocalDate.now();
        this.color = "#000000";
        this.isSystemDefault = false;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public Boolean getIsSystemDefault() {
        return isSystemDefault;
    }

    public void setIsSystemDefault(Boolean isSystemDefault) {
        this.isSystemDefault = isSystemDefault;
    }

    @PrePersist
    private void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDate.now();
        }
    }
}
