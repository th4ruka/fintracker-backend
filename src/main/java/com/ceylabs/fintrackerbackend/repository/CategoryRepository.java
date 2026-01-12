package com.ceylabs.fintrackerbackend.repository;

import com.ceylabs.fintrackerbackend.enums.CategoryType;
import com.ceylabs.fintrackerbackend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find by user
    List<Category> findByUserId(Long userId);

    // Find by category type
    List<Category> findByCategoryType(CategoryType categoryType);

    // Find by user and category type
    List<Category> findByUserIdAndCategoryType(Long userId, CategoryType categoryType);

    // Find system default categories
    List<Category> findByIsSystemDefaultTrue();

    // Find user-created categories (non-system)
    List<Category> findByIsSystemDefaultFalse();

    // Find user-created categories by user
    List<Category> findByUserIdAndIsSystemDefaultFalse(Long userId);

    // Find system default categories by type
    List<Category> findByIsSystemDefaultTrueAndCategoryType(CategoryType categoryType);

    // Find category by name and user (for duplicate checking)
    Optional<Category> findByNameAndUserId(String name, Long userId);

    // Check if category name exists for user
    boolean existsByNameAndUserId(String name, Long userId);

    // Find categories by name containing (search)
    List<Category> findByNameContainingIgnoreCase(String name);

    // Find categories by name containing and user
    List<Category> findByUserIdAndNameContainingIgnoreCase(Long userId, String name);

    // Get all categories accessible by user (system defaults + user's own)
    @Query("SELECT c FROM Category c WHERE c.isSystemDefault = true OR c.user.id = :userId")
    List<Category> findAccessibleCategoriesByUser(@Param("userId") Long userId);

    // Get all categories accessible by user filtered by type
    @Query("SELECT c FROM Category c WHERE (c.isSystemDefault = true OR c.user.id = :userId) AND c.categoryType = :categoryType")
    List<Category> findAccessibleCategoriesByUserAndType(
            @Param("userId") Long userId,
            @Param("categoryType") CategoryType categoryType);

    // Get categories that can be used for expenses (EXPENSE or BOTH)
    @Query("SELECT c FROM Category c WHERE (c.isSystemDefault = true OR c.user.id = :userId) AND (c.categoryType = 'EXPENSE' OR c.categoryType = 'BOTH')")
    List<Category> findExpenseCategoriesByUser(@Param("userId") Long userId);

    // Get categories that can be used for income (INCOME or BOTH)
    @Query("SELECT c FROM Category c WHERE (c.isSystemDefault = true OR c.user.id = :userId) AND (c.categoryType = 'INCOME' OR c.categoryType = 'BOTH')")
    List<Category> findIncomeCategoriesByUser(@Param("userId") Long userId);

    // Count categories by user
    Long countByUserId(Long userId);

    // Count system default categories
    Long countByIsSystemDefaultTrue();

    // Count user-created categories
    Long countByUserIdAndIsSystemDefaultFalse(Long userId);
}
