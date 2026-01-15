package com.ceylabs.fintrackerbackend.service;

import com.ceylabs.fintrackerbackend.dto.CategoryCreateRequest;
import com.ceylabs.fintrackerbackend.dto.CategoryResponse;
import com.ceylabs.fintrackerbackend.enums.CategoryType;
import com.ceylabs.fintrackerbackend.model.Category;
import com.ceylabs.fintrackerbackend.model.User;
import com.ceylabs.fintrackerbackend.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Unit Tests")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CategoryService categoryService;

    private User testUser;
    private Category testCategory;
    private Category systemCategory;
    private CategoryCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setUser(testUser);
        testCategory.setName("Groceries");
        testCategory.setCategoryType(CategoryType.EXPENSE);
        testCategory.setColor("#FF0000");
        testCategory.setIcon("shopping-cart");
        testCategory.setIsSystemDefault(false);
        testCategory.setCreatedDate(LocalDate.now());

        systemCategory = new Category();
        systemCategory.setId(100L);
        systemCategory.setUser(testUser);
        systemCategory.setName("Food");
        systemCategory.setCategoryType(CategoryType.EXPENSE);
        systemCategory.setColor("#00FF00");
        systemCategory.setIcon("food");
        systemCategory.setIsSystemDefault(true);
        systemCategory.setCreatedDate(LocalDate.now());

        createRequest = new CategoryCreateRequest();
        createRequest.setUserId(1L);
        createRequest.setName("Entertainment");
        createRequest.setCategoryType(CategoryType.EXPENSE);
        createRequest.setColor("#0000FF");
        createRequest.setIcon("ticket");
    }

    // ==================== createCategory() Tests ====================

    @Test
    @DisplayName("createCategory - Should create category successfully with valid data")
    void createCategory_WithValidData_ShouldCreateCategory() {
        // Given
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.existsByNameAndUserId("Entertainment", 1L)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // When
        CategoryResponse result = categoryService.createCategory(createRequest);

        // Then
        assertThat(result).isNotNull();
        verify(userService, times(1)).getUserById(1L);
        verify(categoryRepository, times(1)).existsByNameAndUserId("Entertainment", 1L);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("createCategory - Should throw exception when user does not exist")
    void createCategory_WithInvalidUser_ShouldThrowException() {
        // Given
        when(userService.getUserById(999L)).thenReturn(Optional.empty());
        createRequest.setUserId(999L);

        // When & Then
        assertThatThrownBy(() -> categoryService.createCategory(createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User with ID 999 does not exist");

        verify(userService, times(1)).getUserById(999L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("createCategory - Should throw exception when duplicate category name exists")
    void createCategory_WithDuplicateName_ShouldThrowException() {
        // Given
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.existsByNameAndUserId("Entertainment", 1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> categoryService.createCategory(createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Category with name 'Entertainment' already exists for this user");

        verify(userService, times(1)).getUserById(1L);
        verify(categoryRepository, times(1)).existsByNameAndUserId("Entertainment", 1L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("createCategory - Should set isSystemDefault to false for user-created categories")
    void createCategory_ShouldSetSystemDefaultToFalse() {
        // Given
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.existsByNameAndUserId(anyString(), anyLong())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category savedCategory = invocation.getArgument(0);
            assertThat(savedCategory.getIsSystemDefault()).isFalse();
            return savedCategory;
        });

        // When
        categoryService.createCategory(createRequest);

        // Then
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    // ==================== updateCategory() Tests ====================

    @Test
    @DisplayName("updateCategory - Should update category successfully")
    void updateCategory_WithValidData_ShouldUpdateCategory() {
        // Given
        createRequest.setName("Updated Groceries");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findByNameAndUserId("Updated Groceries", 1L)).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // When
        CategoryResponse result = categoryService.updateCategory(1L, createRequest);

        // Then
        assertThat(testCategory.getName()).isEqualTo("Updated Groceries");
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).save(testCategory);
    }

    @Test
    @DisplayName("updateCategory - Should throw exception when category not found")
    void updateCategory_WhenCategoryNotFound_ShouldThrowException() {
        // Given
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(999L, createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Category with ID 999 does not exist");

        verify(categoryRepository, times(1)).findById(999L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("updateCategory - Should throw exception when trying to update system default category")
    void updateCategory_SystemDefaultCategory_ShouldThrowException() {
        // Given
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(systemCategory));

        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(100L, createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot update system default categories");

        verify(categoryRepository, times(1)).findById(100L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("updateCategory - Should throw exception when category belongs to different user")
    void updateCategory_WhenCategoryBelongsToDifferentUser_ShouldThrowException() {
        // Given
        createRequest.setUserId(2L); // Different user

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(1L, createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Category does not belong to the specified user");

        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("updateCategory - Should throw exception when duplicate name exists for different category")
    void updateCategory_WithDuplicateNameForDifferentCategory_ShouldThrowException() {
        // Given
        Category anotherCategory = new Category();
        anotherCategory.setId(2L);
        anotherCategory.setName("Entertainment");

        createRequest.setName("Entertainment");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findByNameAndUserId("Entertainment", 1L)).thenReturn(Optional.of(anotherCategory));

        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(1L, createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Category with name 'Entertainment' already exists for this user");

        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("updateCategory - Should allow updating to same name for same category")
    void updateCategory_WithSameNameForSameCategory_ShouldAllowUpdate() {
        // Given
        createRequest.setName("Groceries"); // Same name

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findByNameAndUserId("Groceries", 1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // When
        CategoryResponse result = categoryService.updateCategory(1L, createRequest);

        // Then
        assertThat(result).isNotNull();
        verify(categoryRepository, times(1)).save(testCategory);
    }

    // ==================== deleteCategory() Tests ====================

    @Test
    @DisplayName("deleteCategory - Should delete category successfully")
    void deleteCategory_WithValidCategory_ShouldDeleteCategory() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        doNothing().when(categoryRepository).delete(testCategory);

        // When
        categoryService.deleteCategory(1L);

        // Then
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).delete(testCategory);
    }

    @Test
    @DisplayName("deleteCategory - Should throw exception when category not found")
    void deleteCategory_WhenCategoryNotFound_ShouldThrowException() {
        // Given
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.deleteCategory(999L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Category with ID 999 does not exist");

        verify(categoryRepository, times(1)).findById(999L);
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    @DisplayName("deleteCategory - Should throw exception when trying to delete system default category")
    void deleteCategory_SystemDefaultCategory_ShouldThrowException() {
        // Given
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(systemCategory));

        // When & Then
        assertThatThrownBy(() -> categoryService.deleteCategory(100L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete system default categories");

        verify(categoryRepository, times(1)).findById(100L);
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    // ==================== getCategoryById() Tests ====================

    @Test
    @DisplayName("getCategoryById - Should return category when category exists")
    void getCategoryById_WhenCategoryExists_ShouldReturnCategory() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // When
        Optional<Category> result = categoryService.getCategoryById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("Groceries");
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getCategoryById - Should return empty optional when category does not exist")
    void getCategoryById_WhenCategoryDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Category> result = categoryService.getCategoryById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(categoryRepository, times(1)).findById(999L);
    }

    // ==================== getAccessibleCategoriesByUser() Tests ====================

    @Test
    @DisplayName("getAccessibleCategoriesByUser - Should return user categories and system defaults")
    void getAccessibleCategoriesByUser_ShouldReturnAllAccessibleCategories() {
        // Given
        when(categoryRepository.findAccessibleCategoriesByUser(1L))
                .thenReturn(Arrays.asList(testCategory, systemCategory));

        // When
        List<CategoryResponse> result = categoryService.getAccessibleCategoriesByUser(1L);

        // Then
        assertThat(result).hasSize(2);
        verify(categoryRepository, times(1)).findAccessibleCategoriesByUser(1L);
    }

    // ==================== getAccessibleCategoriesByUserAndType() Tests ====================

    @Test
    @DisplayName("getAccessibleCategoriesByUserAndType - Should return categories filtered by type")
    void getAccessibleCategoriesByUserAndType_ShouldReturnFilteredCategories() {
        // Given
        when(categoryRepository.findAccessibleCategoriesByUserAndType(1L, CategoryType.EXPENSE))
                .thenReturn(Arrays.asList(testCategory, systemCategory));

        // When
        List<CategoryResponse> result = categoryService.getAccessibleCategoriesByUserAndType(1L, CategoryType.EXPENSE);

        // Then
        assertThat(result).hasSize(2);
        verify(categoryRepository, times(1)).findAccessibleCategoriesByUserAndType(1L, CategoryType.EXPENSE);
    }

    // ==================== getExpenseCategoriesByUser() Tests ====================

    @Test
    @DisplayName("getExpenseCategoriesByUser - Should return expense categories only")
    void getExpenseCategoriesByUser_ShouldReturnExpenseCategories() {
        // Given
        when(categoryRepository.findExpenseCategoriesByUser(1L))
                .thenReturn(Arrays.asList(testCategory, systemCategory));

        // When
        List<CategoryResponse> result = categoryService.getExpenseCategoriesByUser(1L);

        // Then
        assertThat(result).hasSize(2);
        verify(categoryRepository, times(1)).findExpenseCategoriesByUser(1L);
    }

    // ==================== getIncomeCategoriesByUser() Tests ====================

    @Test
    @DisplayName("getIncomeCategoriesByUser - Should return income categories only")
    void getIncomeCategoriesByUser_ShouldReturnIncomeCategories() {
        // Given
        Category incomeCategory = new Category();
        incomeCategory.setId(2L);
        incomeCategory.setUser(testUser);
        incomeCategory.setName("Salary");
        incomeCategory.setCategoryType(CategoryType.INCOME);

        when(categoryRepository.findIncomeCategoriesByUser(1L))
                .thenReturn(Arrays.asList(incomeCategory));

        // When
        List<CategoryResponse> result = categoryService.getIncomeCategoriesByUser(1L);

        // Then
        assertThat(result).hasSize(1);
        verify(categoryRepository, times(1)).findIncomeCategoriesByUser(1L);
    }

    // ==================== getUserCreatedCategories() Tests ====================

    @Test
    @DisplayName("getUserCreatedCategories - Should return only user-created categories (exclude system defaults)")
    void getUserCreatedCategories_ShouldReturnOnlyUserCreatedCategories() {
        // Given
        when(categoryRepository.findByUserIdAndIsSystemDefaultFalse(1L))
                .thenReturn(Arrays.asList(testCategory));

        // When
        List<CategoryResponse> result = categoryService.getUserCreatedCategories(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Groceries");
        verify(categoryRepository, times(1)).findByUserIdAndIsSystemDefaultFalse(1L);
    }

    // ==================== getSystemDefaultCategories() Tests ====================

    @Test
    @DisplayName("getSystemDefaultCategories - Should return only system default categories")
    void getSystemDefaultCategories_ShouldReturnSystemDefaultCategories() {
        // Given
        when(categoryRepository.findByIsSystemDefaultTrue())
                .thenReturn(Arrays.asList(systemCategory));

        // When
        List<CategoryResponse> result = categoryService.getSystemDefaultCategories();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Food");
        assertThat(result.get(0).getIsSystemDefault()).isTrue();
        verify(categoryRepository, times(1)).findByIsSystemDefaultTrue();
    }

    // ==================== searchCategoriesByName() Tests ====================

    @Test
    @DisplayName("searchCategoriesByName - Should return categories matching search term")
    void searchCategoriesByName_ShouldReturnMatchingCategories() {
        // Given
        when(categoryRepository.findByUserIdAndNameContainingIgnoreCase(1L, "Groc"))
                .thenReturn(Arrays.asList(testCategory));

        // When
        List<CategoryResponse> result = categoryService.searchCategoriesByName(1L, "Groc");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Groceries");
        verify(categoryRepository, times(1)).findByUserIdAndNameContainingIgnoreCase(1L, "Groc");
    }

    @Test
    @DisplayName("searchCategoriesByName - Should return empty list when no matches found")
    void searchCategoriesByName_WhenNoMatches_ShouldReturnEmptyList() {
        // Given
        when(categoryRepository.findByUserIdAndNameContainingIgnoreCase(1L, "NonExistent"))
                .thenReturn(Arrays.asList());

        // When
        List<CategoryResponse> result = categoryService.searchCategoriesByName(1L, "NonExistent");

        // Then
        assertThat(result).isEmpty();
        verify(categoryRepository, times(1)).findByUserIdAndNameContainingIgnoreCase(1L, "NonExistent");
    }

    // ==================== mapToResponse() Tests ====================

    @Test
    @DisplayName("mapToResponse - Should map category entity to response DTO correctly")
    void mapToResponse_ShouldMapCorrectly() {
        // When
        CategoryResponse result = categoryService.mapToResponse(testCategory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testCategory.getId());
        assertThat(result.getUserId()).isEqualTo(testCategory.getUser().getId());
        assertThat(result.getName()).isEqualTo(testCategory.getName());
        assertThat(result.getCategoryType()).isEqualTo(testCategory.getCategoryType());
        assertThat(result.getColor()).isEqualTo(testCategory.getColor());
        assertThat(result.getIcon()).isEqualTo(testCategory.getIcon());
        assertThat(result.getIsSystemDefault()).isEqualTo(testCategory.getIsSystemDefault());
    }

    @Test
    @DisplayName("mapToResponse - Should map system default category correctly")
    void mapToResponse_SystemDefaultCategory_ShouldMapCorrectly() {
        // When
        CategoryResponse result = categoryService.mapToResponse(systemCategory);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsSystemDefault()).isTrue();
        assertThat(result.getName()).isEqualTo("Food");
    }
}
