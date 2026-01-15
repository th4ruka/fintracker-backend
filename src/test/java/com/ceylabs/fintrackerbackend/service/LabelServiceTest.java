package com.ceylabs.fintrackerbackend.service;

import com.ceylabs.fintrackerbackend.dto.LabelCreateRequest;
import com.ceylabs.fintrackerbackend.dto.LabelResponse;
import com.ceylabs.fintrackerbackend.model.Label;
import com.ceylabs.fintrackerbackend.model.User;
import com.ceylabs.fintrackerbackend.repository.LabelRepository;
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
@DisplayName("LabelService Unit Tests")
class LabelServiceTest {

    @Mock
    private LabelRepository labelRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private LabelService labelService;

    private User testUser;
    private Label testLabel;
    private LabelCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");

        testLabel = new Label();
        testLabel.setId(1L);
        testLabel.setUser(testUser);
        testLabel.setName("Work");
        testLabel.setColor("#FF0000");
        testLabel.setCreatedDate(LocalDate.now());

        createRequest = new LabelCreateRequest();
        createRequest.setUserId(1L);
        createRequest.setName("Personal");
        createRequest.setColor("#0000FF");
    }

    // ==================== createLabel() Tests ====================

    @Test
    @DisplayName("createLabel - Should create label successfully with valid data")
    void createLabel_WithValidData_ShouldCreateLabel() {
        // Given
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(labelRepository.existsByNameAndUserId("Personal", 1L)).thenReturn(false);
        when(labelRepository.save(any(Label.class))).thenReturn(testLabel);

        // When
        LabelResponse result = labelService.createLabel(createRequest);

        // Then
        assertThat(result).isNotNull();
        verify(userService, times(1)).getUserById(1L);
        verify(labelRepository, times(1)).existsByNameAndUserId("Personal", 1L);
        verify(labelRepository, times(1)).save(any(Label.class));
    }

    @Test
    @DisplayName("createLabel - Should throw exception when user does not exist")
    void createLabel_WithInvalidUser_ShouldThrowException() {
        // Given
        when(userService.getUserById(999L)).thenReturn(Optional.empty());
        createRequest.setUserId(999L);

        // When & Then
        assertThatThrownBy(() -> labelService.createLabel(createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User with ID 999 does not exist");

        verify(userService, times(1)).getUserById(999L);
        verify(labelRepository, never()).save(any(Label.class));
    }

    @Test
    @DisplayName("createLabel - Should throw exception when duplicate label name exists")
    void createLabel_WithDuplicateName_ShouldThrowException() {
        // Given
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(labelRepository.existsByNameAndUserId("Personal", 1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> labelService.createLabel(createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Label with name 'Personal' already exists for this user");

        verify(userService, times(1)).getUserById(1L);
        verify(labelRepository, times(1)).existsByNameAndUserId("Personal", 1L);
        verify(labelRepository, never()).save(any(Label.class));
    }

    // ==================== updateLabel() Tests ====================

    @Test
    @DisplayName("updateLabel - Should update label successfully")
    void updateLabel_WithValidData_ShouldUpdateLabel() {
        // Given
        createRequest.setName("Updated Work");

        when(labelRepository.findById(1L)).thenReturn(Optional.of(testLabel));
        when(labelRepository.findByNameAndUserId("Updated Work", 1L)).thenReturn(Optional.empty());
        when(labelRepository.save(any(Label.class))).thenReturn(testLabel);

        // When
        LabelResponse result = labelService.updateLabel(1L, createRequest);

        // Then
        assertThat(testLabel.getName()).isEqualTo("Updated Work");
        verify(labelRepository, times(1)).findById(1L);
        verify(labelRepository, times(1)).save(testLabel);
    }

    @Test
    @DisplayName("updateLabel - Should throw exception when label not found")
    void updateLabel_WhenLabelNotFound_ShouldThrowException() {
        // Given
        when(labelRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> labelService.updateLabel(999L, createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Label with ID 999 does not exist");

        verify(labelRepository, times(1)).findById(999L);
        verify(labelRepository, never()).save(any(Label.class));
    }

    @Test
    @DisplayName("updateLabel - Should throw exception when label belongs to different user")
    void updateLabel_WhenLabelBelongsToDifferentUser_ShouldThrowException() {
        // Given
        createRequest.setUserId(2L); // Different user

        when(labelRepository.findById(1L)).thenReturn(Optional.of(testLabel));

        // When & Then
        assertThatThrownBy(() -> labelService.updateLabel(1L, createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Label does not belong to the specified user");

        verify(labelRepository, times(1)).findById(1L);
        verify(labelRepository, never()).save(any(Label.class));
    }

    @Test
    @DisplayName("updateLabel - Should throw exception when duplicate name exists for different label")
    void updateLabel_WithDuplicateNameForDifferentLabel_ShouldThrowException() {
        // Given
        Label anotherLabel = new Label();
        anotherLabel.setId(2L);
        anotherLabel.setName("Personal");

        createRequest.setName("Personal");

        when(labelRepository.findById(1L)).thenReturn(Optional.of(testLabel));
        when(labelRepository.findByNameAndUserId("Personal", 1L)).thenReturn(Optional.of(anotherLabel));

        // When & Then
        assertThatThrownBy(() -> labelService.updateLabel(1L, createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Label with name 'Personal' already exists for this user");

        verify(labelRepository, times(1)).findById(1L);
        verify(labelRepository, never()).save(any(Label.class));
    }

    @Test
    @DisplayName("updateLabel - Should allow updating to same name for same label")
    void updateLabel_WithSameNameForSameLabel_ShouldAllowUpdate() {
        // Given
        createRequest.setName("Work"); // Same name

        when(labelRepository.findById(1L)).thenReturn(Optional.of(testLabel));
        when(labelRepository.findByNameAndUserId("Work", 1L)).thenReturn(Optional.of(testLabel));
        when(labelRepository.save(any(Label.class))).thenReturn(testLabel);

        // When
        LabelResponse result = labelService.updateLabel(1L, createRequest);

        // Then
        assertThat(result).isNotNull();
        verify(labelRepository, times(1)).save(testLabel);
    }

    @Test
    @DisplayName("updateLabel - Should update label color successfully")
    void updateLabel_WithNewColor_ShouldUpdateColor() {
        // Given
        createRequest.setColor("#00FF00");

        when(labelRepository.findById(1L)).thenReturn(Optional.of(testLabel));
        when(labelRepository.findByNameAndUserId(anyString(), anyLong())).thenReturn(Optional.empty());
        when(labelRepository.save(any(Label.class))).thenReturn(testLabel);

        // When
        labelService.updateLabel(1L, createRequest);

        // Then
        assertThat(testLabel.getColor()).isEqualTo("#00FF00");
        verify(labelRepository, times(1)).save(testLabel);
    }

    // ==================== deleteLabel() Tests ====================

    @Test
    @DisplayName("deleteLabel - Should delete label successfully")
    void deleteLabel_WithValidLabel_ShouldDeleteLabel() {
        // Given
        when(labelRepository.findById(1L)).thenReturn(Optional.of(testLabel));
        doNothing().when(labelRepository).delete(testLabel);

        // When
        labelService.deleteLabel(1L);

        // Then
        verify(labelRepository, times(1)).findById(1L);
        verify(labelRepository, times(1)).delete(testLabel);
    }

    @Test
    @DisplayName("deleteLabel - Should throw exception when label not found")
    void deleteLabel_WhenLabelNotFound_ShouldThrowException() {
        // Given
        when(labelRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> labelService.deleteLabel(999L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Label with ID 999 does not exist");

        verify(labelRepository, times(1)).findById(999L);
        verify(labelRepository, never()).delete(any(Label.class));
    }

    // ==================== getLabelById() Tests ====================

    @Test
    @DisplayName("getLabelById - Should return label when label exists")
    void getLabelById_WhenLabelExists_ShouldReturnLabel() {
        // Given
        when(labelRepository.findById(1L)).thenReturn(Optional.of(testLabel));

        // When
        Optional<Label> result = labelService.getLabelById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("Work");
        verify(labelRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getLabelById - Should return empty optional when label does not exist")
    void getLabelById_WhenLabelDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(labelRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Label> result = labelService.getLabelById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(labelRepository, times(1)).findById(999L);
    }

    // ==================== getLabelsByUser() Tests ====================

    @Test
    @DisplayName("getLabelsByUser - Should return all labels for a user")
    void getLabelsByUser_ShouldReturnAllLabels() {
        // Given
        Label label2 = new Label();
        label2.setId(2L);
        label2.setUser(testUser);
        label2.setName("Personal");
        label2.setColor("#0000FF");

        when(labelRepository.findByUserId(1L)).thenReturn(Arrays.asList(testLabel, label2));

        // When
        List<LabelResponse> result = labelService.getLabelsByUser(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Work");
        assertThat(result.get(1).getName()).isEqualTo("Personal");
        verify(labelRepository, times(1)).findByUserId(1L);
    }

    @Test
    @DisplayName("getLabelsByUser - Should return empty list when user has no labels")
    void getLabelsByUser_WhenNoLabels_ShouldReturnEmptyList() {
        // Given
        when(labelRepository.findByUserId(1L)).thenReturn(Arrays.asList());

        // When
        List<LabelResponse> result = labelService.getLabelsByUser(1L);

        // Then
        assertThat(result).isEmpty();
        verify(labelRepository, times(1)).findByUserId(1L);
    }

    // ==================== getUsedLabelsByUser() Tests ====================

    @Test
    @DisplayName("getUsedLabelsByUser - Should return labels that are attached to records")
    void getUsedLabelsByUser_ShouldReturnUsedLabels() {
        // Given
        when(labelRepository.findUsedLabelsByUser(1L)).thenReturn(Arrays.asList(testLabel));

        // When
        List<LabelResponse> result = labelService.getUsedLabelsByUser(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Work");
        verify(labelRepository, times(1)).findUsedLabelsByUser(1L);
    }

    @Test
    @DisplayName("getUsedLabelsByUser - Should return empty list when no labels are used")
    void getUsedLabelsByUser_WhenNoUsedLabels_ShouldReturnEmptyList() {
        // Given
        when(labelRepository.findUsedLabelsByUser(1L)).thenReturn(Arrays.asList());

        // When
        List<LabelResponse> result = labelService.getUsedLabelsByUser(1L);

        // Then
        assertThat(result).isEmpty();
        verify(labelRepository, times(1)).findUsedLabelsByUser(1L);
    }

    // ==================== getUnusedLabelsByUser() Tests ====================

    @Test
    @DisplayName("getUnusedLabelsByUser - Should return labels that are not attached to any record")
    void getUnusedLabelsByUser_ShouldReturnUnusedLabels() {
        // Given
        when(labelRepository.findUnusedLabelsByUser(1L)).thenReturn(Arrays.asList(testLabel));

        // When
        List<LabelResponse> result = labelService.getUnusedLabelsByUser(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Work");
        verify(labelRepository, times(1)).findUnusedLabelsByUser(1L);
    }

    @Test
    @DisplayName("getUnusedLabelsByUser - Should return empty list when all labels are used")
    void getUnusedLabelsByUser_WhenAllLabelsUsed_ShouldReturnEmptyList() {
        // Given
        when(labelRepository.findUnusedLabelsByUser(1L)).thenReturn(Arrays.asList());

        // When
        List<LabelResponse> result = labelService.getUnusedLabelsByUser(1L);

        // Then
        assertThat(result).isEmpty();
        verify(labelRepository, times(1)).findUnusedLabelsByUser(1L);
    }

    // ==================== searchLabelsByName() Tests ====================

    @Test
    @DisplayName("searchLabelsByName - Should return labels matching search term")
    void searchLabelsByName_ShouldReturnMatchingLabels() {
        // Given
        when(labelRepository.findByUserIdAndNameContainingIgnoreCase(1L, "Wor"))
                .thenReturn(Arrays.asList(testLabel));

        // When
        List<LabelResponse> result = labelService.searchLabelsByName(1L, "Wor");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Work");
        verify(labelRepository, times(1)).findByUserIdAndNameContainingIgnoreCase(1L, "Wor");
    }

    @Test
    @DisplayName("searchLabelsByName - Should return empty list when no matches found")
    void searchLabelsByName_WhenNoMatches_ShouldReturnEmptyList() {
        // Given
        when(labelRepository.findByUserIdAndNameContainingIgnoreCase(1L, "NonExistent"))
                .thenReturn(Arrays.asList());

        // When
        List<LabelResponse> result = labelService.searchLabelsByName(1L, "NonExistent");

        // Then
        assertThat(result).isEmpty();
        verify(labelRepository, times(1)).findByUserIdAndNameContainingIgnoreCase(1L, "NonExistent");
    }

    // ==================== getLabelsByColor() Tests ====================

    @Test
    @DisplayName("getLabelsByColor - Should return labels with specific color")
    void getLabelsByColor_ShouldReturnLabelsWithColor() {
        // Given
        when(labelRepository.findByUserIdAndColor(1L, "#FF0000"))
                .thenReturn(Arrays.asList(testLabel));

        // When
        List<LabelResponse> result = labelService.getLabelsByColor(1L, "#FF0000");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getColor()).isEqualTo("#FF0000");
        verify(labelRepository, times(1)).findByUserIdAndColor(1L, "#FF0000");
    }

    @Test
    @DisplayName("getLabelsByColor - Should return empty list when no labels with specified color")
    void getLabelsByColor_WhenNoLabelsWithColor_ShouldReturnEmptyList() {
        // Given
        when(labelRepository.findByUserIdAndColor(1L, "#00FF00"))
                .thenReturn(Arrays.asList());

        // When
        List<LabelResponse> result = labelService.getLabelsByColor(1L, "#00FF00");

        // Then
        assertThat(result).isEmpty();
        verify(labelRepository, times(1)).findByUserIdAndColor(1L, "#00FF00");
    }

    // ==================== mapToResponse() Tests ====================

    @Test
    @DisplayName("mapToResponse - Should map label entity to response DTO correctly")
    void mapToResponse_ShouldMapCorrectly() {
        // When
        LabelResponse result = labelService.mapToResponse(testLabel);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testLabel.getId());
        assertThat(result.getUserId()).isEqualTo(testLabel.getUser().getId());
        assertThat(result.getName()).isEqualTo(testLabel.getName());
        assertThat(result.getColor()).isEqualTo(testLabel.getColor());
        assertThat(result.getCreatedDate()).isEqualTo(testLabel.getCreatedDate());
    }
}
