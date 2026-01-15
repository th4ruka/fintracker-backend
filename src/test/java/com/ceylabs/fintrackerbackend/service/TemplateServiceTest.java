package com.ceylabs.fintrackerbackend.service;

import com.ceylabs.fintrackerbackend.dto.*;
import com.ceylabs.fintrackerbackend.enums.RecordType;
import com.ceylabs.fintrackerbackend.model.*;
import com.ceylabs.fintrackerbackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TemplateService Unit Tests")
class TemplateServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private LabelRepository labelRepository;

    @Mock
    private UserService userService;

    @Mock
    private FinancialRecordService financialRecordService;

    @InjectMocks
    private TemplateService templateService;

    private User testUser;
    private Account testAccount;
    private Account toAccount;
    private Category testCategory;
    private Label testLabel;
    private Template testTemplate;
    private TemplateCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setName("Checking Account");
        testAccount.setBalance(BigDecimal.valueOf(1000.00));
        testAccount.setUser(testUser);
        testAccount.setCurrency("USD");

        toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setName("Savings Account");
        toAccount.setBalance(BigDecimal.valueOf(5000.00));
        toAccount.setUser(testUser);
        toAccount.setCurrency("USD");

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Groceries");
        testCategory.setUser(testUser);

        testLabel = new Label();
        testLabel.setId(1L);
        testLabel.setName("Work");
        testLabel.setUser(testUser);

        testTemplate = new Template();
        testTemplate.setId(1L);
        testTemplate.setUser(testUser);
        testTemplate.setName("Monthly Rent");
        testTemplate.setAmount(BigDecimal.valueOf(1000.00));
        testTemplate.setRecordType(RecordType.EXPENSE);
        testTemplate.setCategory(testCategory);
        testTemplate.setAccount(testAccount);
        testTemplate.setLabels(new HashSet<>(Arrays.asList(testLabel)));

        createRequest = new TemplateCreateRequest();
        createRequest.setUserId(1L);
        createRequest.setName("Salary Template");
        createRequest.setAmount(BigDecimal.valueOf(5000.00));
        createRequest.setRecordType(RecordType.INCOME);
        createRequest.setAccountId(1L);
        createRequest.setCategoryId(1L);
        createRequest.setLabelIds(new HashSet<>(Arrays.asList(1L)));
    }

    // ==================== createTemplate() Tests ====================

    @Test
    @DisplayName("createTemplate - Should create expense template successfully")
    void createTemplate_WithValidExpenseData_ShouldCreateTemplate() {
        // Given
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(templateRepository.existsByNameAndUserId("Salary Template", 1L)).thenReturn(false);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(labelRepository.findById(1L)).thenReturn(Optional.of(testLabel));
        when(templateRepository.save(any(Template.class))).thenReturn(testTemplate);
        when(templateRepository.countTemplateUsage(anyLong())).thenReturn(0L);

        // When
        TemplateResponse result = templateService.createTemplate(createRequest);

        // Then
        assertThat(result).isNotNull();
        verify(userService, times(1)).getUserById(1L);
        verify(templateRepository, times(1)).save(any(Template.class));
    }

    @Test
    @DisplayName("createTemplate - Should create transfer template successfully")
    void createTemplate_WithValidTransferData_ShouldCreateTemplate() {
        // Given
        createRequest.setRecordType(RecordType.TRANSFER);
        createRequest.setToAccountId(2L);

        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(templateRepository.existsByNameAndUserId(anyString(), anyLong())).thenReturn(false);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(labelRepository.findById(1L)).thenReturn(Optional.of(testLabel));
        when(templateRepository.save(any(Template.class))).thenReturn(testTemplate);
        when(templateRepository.countTemplateUsage(anyLong())).thenReturn(0L);

        // When
        TemplateResponse result = templateService.createTemplate(createRequest);

        // Then
        assertThat(result).isNotNull();
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).findById(2L);
        verify(templateRepository, times(1)).save(any(Template.class));
    }

    @Test
    @DisplayName("createTemplate - Should throw exception when user does not exist")
    void createTemplate_WithInvalidUser_ShouldThrowException() {
        // Given
        when(userService.getUserById(999L)).thenReturn(Optional.empty());
        createRequest.setUserId(999L);

        // When & Then
        assertThatThrownBy(() -> templateService.createTemplate(createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User with ID 999 does not exist");

        verify(userService, times(1)).getUserById(999L);
        verify(templateRepository, never()).save(any(Template.class));
    }

    @Test
    @DisplayName("createTemplate - Should throw exception when duplicate template name exists")
    void createTemplate_WithDuplicateName_ShouldThrowException() {
        // Given
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(templateRepository.existsByNameAndUserId("Salary Template", 1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> templateService.createTemplate(createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Template with name 'Salary Template' already exists for this user");

        verify(templateRepository, never()).save(any(Template.class));
    }

    @Test
    @DisplayName("createTemplate - Should throw exception when account does not exist")
    void createTemplate_WithInvalidAccount_ShouldThrowException() {
        // Given
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(templateRepository.existsByNameAndUserId(anyString(), anyLong())).thenReturn(false);
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());
        createRequest.setAccountId(999L);

        // When & Then
        assertThatThrownBy(() -> templateService.createTemplate(createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Account with ID 999 does not exist");

        verify(templateRepository, never()).save(any(Template.class));
    }

    @Test
    @DisplayName("createTemplate - Should throw exception when account belongs to different user")
    void createTemplate_WithAccountOfDifferentUser_ShouldThrowException() {
        // Given
        User anotherUser = new User();
        anotherUser.setId(2L);
        testAccount.setUser(anotherUser);

        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(templateRepository.existsByNameAndUserId(anyString(), anyLong())).thenReturn(false);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // When & Then
        assertThatThrownBy(() -> templateService.createTemplate(createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Account does not belong to the specified user");

        verify(templateRepository, never()).save(any(Template.class));
    }

    @Test
    @DisplayName("createTemplate - Should throw exception when transfer template missing toAccountId")
    void createTemplate_TransferWithoutToAccount_ShouldThrowException() {
        // Given
        createRequest.setRecordType(RecordType.TRANSFER);
        createRequest.setToAccountId(null);

        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(templateRepository.existsByNameAndUserId(anyString(), anyLong())).thenReturn(false);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // When & Then
        assertThatThrownBy(() -> templateService.createTemplate(createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("To account is required for transfer templates");

        verify(templateRepository, never()).save(any(Template.class));
    }

    @Test
    @DisplayName("createTemplate - Should throw exception when transferring to same account")
    void createTemplate_TransferToSameAccount_ShouldThrowException() {
        // Given
        createRequest.setRecordType(RecordType.TRANSFER);
        createRequest.setAccountId(1L);
        createRequest.setToAccountId(1L); // Same account

        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(templateRepository.existsByNameAndUserId(anyString(), anyLong())).thenReturn(false);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // When & Then
        assertThatThrownBy(() -> templateService.createTemplate(createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot transfer to the same account");

        verify(templateRepository, never()).save(any(Template.class));
    }

    // ==================== updateTemplate() Tests ====================

    @Test
    @DisplayName("updateTemplate - Should update template successfully")
    void updateTemplate_WithValidData_ShouldUpdateTemplate() {
        // Given
        createRequest.setName("Updated Template");

        when(templateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(templateRepository.findByNameAndUserId("Updated Template", 1L)).thenReturn(Optional.empty());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(labelRepository.findById(1L)).thenReturn(Optional.of(testLabel));
        when(templateRepository.save(any(Template.class))).thenReturn(testTemplate);
        when(templateRepository.countTemplateUsage(anyLong())).thenReturn(0L);

        // When
        TemplateResponse result = templateService.updateTemplate(1L, createRequest);

        // Then
        assertThat(testTemplate.getName()).isEqualTo("Updated Template");
        verify(templateRepository, times(1)).save(testTemplate);
    }

    @Test
    @DisplayName("updateTemplate - Should throw exception when template not found")
    void updateTemplate_WhenTemplateNotFound_ShouldThrowException() {
        // Given
        when(templateRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> templateService.updateTemplate(999L, createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Template with ID 999 does not exist");

        verify(templateRepository, never()).save(any(Template.class));
    }

    @Test
    @DisplayName("updateTemplate - Should throw exception when template belongs to different user")
    void updateTemplate_WhenTemplateBelongsToDifferentUser_ShouldThrowException() {
        // Given
        createRequest.setUserId(2L);

        when(templateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));

        // When & Then
        assertThatThrownBy(() -> templateService.updateTemplate(1L, createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Template does not belong to the specified user");

        verify(templateRepository, never()).save(any(Template.class));
    }

    // ==================== deleteTemplate() Tests ====================

    @Test
    @DisplayName("deleteTemplate - Should delete template successfully")
    void deleteTemplate_WithValidTemplate_ShouldDeleteTemplate() {
        // Given
        when(templateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        doNothing().when(templateRepository).delete(testTemplate);

        // When
        templateService.deleteTemplate(1L);

        // Then
        verify(templateRepository, times(1)).findById(1L);
        verify(templateRepository, times(1)).delete(testTemplate);
    }

    @Test
    @DisplayName("deleteTemplate - Should throw exception when template not found")
    void deleteTemplate_WhenTemplateNotFound_ShouldThrowException() {
        // Given
        when(templateRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> templateService.deleteTemplate(999L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Template with ID 999 does not exist");

        verify(templateRepository, never()).delete(any(Template.class));
    }

    // ==================== createRecordFromTemplate() Tests ====================

    @Test
    @DisplayName("createRecordFromTemplate - Should create record from template with defaults")
    void createRecordFromTemplate_WithDefaults_ShouldCreateRecord() {
        // Given
        FinancialRecordResponse expectedResponse = new FinancialRecordResponse();
        expectedResponse.setId(1L);

        when(templateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(financialRecordService.createRecord(any(FinancialRecordCreateRequest.class)))
                .thenReturn(expectedResponse);

        // When
        FinancialRecordResponse result = templateService.createRecordFromTemplate(1L, null, null, null, null);

        // Then
        assertThat(result).isNotNull();
        verify(financialRecordService, times(1)).createRecord(any(FinancialRecordCreateRequest.class));
    }

    @Test
    @DisplayName("createRecordFromTemplate - Should create record with amount override")
    void createRecordFromTemplate_WithAmountOverride_ShouldUseOverrideAmount() {
        // Given
        FinancialRecordResponse expectedResponse = new FinancialRecordResponse();

        when(templateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(financialRecordService.createRecord(any(FinancialRecordCreateRequest.class)))
                .thenReturn(expectedResponse);

        // When
        BigDecimal overrideAmount = BigDecimal.valueOf(2000.00);
        templateService.createRecordFromTemplate(1L, overrideAmount, null, null, null);

        // Then
        verify(financialRecordService, times(1)).createRecord(argThat(request ->
                request.getAmount().compareTo(overrideAmount) == 0
        ));
    }

    @Test
    @DisplayName("createRecordFromTemplate - Should create record with account override")
    void createRecordFromTemplate_WithAccountOverride_ShouldUseOverrideAccount() {
        // Given
        FinancialRecordResponse expectedResponse = new FinancialRecordResponse();

        when(templateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));
        when(financialRecordService.createRecord(any(FinancialRecordCreateRequest.class)))
                .thenReturn(expectedResponse);

        // When
        templateService.createRecordFromTemplate(1L, null, null, 2L, null);

        // Then
        verify(financialRecordService, times(1)).createRecord(argThat(request ->
                request.getAccountId().equals(2L)
        ));
    }

    @Test
    @DisplayName("createRecordFromTemplate - Should throw exception when template not found")
    void createRecordFromTemplate_WhenTemplateNotFound_ShouldThrowException() {
        // Given
        when(templateRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> templateService.createRecordFromTemplate(999L, null, null, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Template with ID 999 does not exist");

        verify(financialRecordService, never()).createRecord(any());
    }

    @Test
    @DisplayName("createRecordFromTemplate - Should throw exception when account not specified")
    void createRecordFromTemplate_WithoutAccount_ShouldThrowException() {
        // Given
        testTemplate.setAccount(null);

        when(templateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));

        // When & Then
        assertThatThrownBy(() -> templateService.createRecordFromTemplate(1L, null, null, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Account must be specified either in template or as override");

        verify(financialRecordService, never()).createRecord(any());
    }

    // ==================== getTemplateById() Tests ====================

    @Test
    @DisplayName("getTemplateById - Should return template when template exists")
    void getTemplateById_WhenTemplateExists_ShouldReturnTemplate() {
        // Given
        when(templateRepository.findById(1L)).thenReturn(Optional.of(testTemplate));

        // When
        Optional<Template> result = templateService.getTemplateById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(templateRepository, times(1)).findById(1L);
    }

    // ==================== getTemplatesByUser() Tests ====================

    @Test
    @DisplayName("getTemplatesByUser - Should return all templates for a user")
    void getTemplatesByUser_ShouldReturnAllTemplates() {
        // Given
        when(templateRepository.findByUserId(1L)).thenReturn(Arrays.asList(testTemplate));
        when(templateRepository.countTemplateUsage(anyLong())).thenReturn(0L);

        // When
        List<TemplateResponse> result = templateService.getTemplatesByUser(1L);

        // Then
        assertThat(result).hasSize(1);
        verify(templateRepository, times(1)).findByUserId(1L);
    }

    // ==================== getTemplatesByUserAndType() Tests ====================

    @Test
    @DisplayName("getTemplatesByUserAndType - Should return templates filtered by type")
    void getTemplatesByUserAndType_ShouldReturnFilteredTemplates() {
        // Given
        when(templateRepository.findByUserIdAndRecordType(1L, RecordType.EXPENSE))
                .thenReturn(Arrays.asList(testTemplate));
        when(templateRepository.countTemplateUsage(anyLong())).thenReturn(0L);

        // When
        List<TemplateResponse> result = templateService.getTemplatesByUserAndType(1L, RecordType.EXPENSE);

        // Then
        assertThat(result).hasSize(1);
        verify(templateRepository, times(1)).findByUserIdAndRecordType(1L, RecordType.EXPENSE);
    }

    // ==================== searchTemplatesByName() Tests ====================

    @Test
    @DisplayName("searchTemplatesByName - Should return templates matching search term")
    void searchTemplatesByName_ShouldReturnMatchingTemplates() {
        // Given
        when(templateRepository.findByUserIdAndNameContainingIgnoreCase(1L, "Rent"))
                .thenReturn(Arrays.asList(testTemplate));
        when(templateRepository.countTemplateUsage(anyLong())).thenReturn(0L);

        // When
        List<TemplateResponse> result = templateService.searchTemplatesByName(1L, "Rent");

        // Then
        assertThat(result).hasSize(1);
        verify(templateRepository, times(1)).findByUserIdAndNameContainingIgnoreCase(1L, "Rent");
    }

    // ==================== getTemplateUsageCount() Tests ====================

    @Test
    @DisplayName("getTemplateUsageCount - Should return usage count")
    void getTemplateUsageCount_ShouldReturnCount() {
        // Given
        when(templateRepository.countTemplateUsage(1L)).thenReturn(5L);

        // When
        Long result = templateService.getTemplateUsageCount(1L);

        // Then
        assertThat(result).isEqualTo(5L);
        verify(templateRepository, times(1)).countTemplateUsage(1L);
    }
}
