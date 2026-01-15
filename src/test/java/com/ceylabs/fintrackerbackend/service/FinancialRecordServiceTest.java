package com.ceylabs.fintrackerbackend.service;

import com.ceylabs.fintrackerbackend.dto.FinancialRecordCreateRequest;
import com.ceylabs.fintrackerbackend.dto.FinancialRecordResponse;
import com.ceylabs.fintrackerbackend.dto.FinancialRecordUpdateRequest;
import com.ceylabs.fintrackerbackend.enums.RecordType;
import com.ceylabs.fintrackerbackend.model.*;
import com.ceylabs.fintrackerbackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
@DisplayName("FinancialRecordService Unit Tests")
class FinancialRecordServiceTest {

    @Mock
    private FinancialRecordRepository financialRecordRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private LabelRepository labelRepository;

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private FinancialRecordService financialRecordService;

    private User testUser;
    private Account testAccount;
    private Account toAccount;
    private Category testCategory;
    private Label testLabel;
    private FinancialRecord testRecord;
    private FinancialRecordCreateRequest createRequest;

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

        testRecord = new FinancialRecord();
        testRecord.setId(1L);
        testRecord.setUser(testUser);
        testRecord.setAccount(testAccount);
        testRecord.setAmount(BigDecimal.valueOf(100.00));
        testRecord.setCurrency("USD");
        testRecord.setRecordType(RecordType.EXPENSE);
        testRecord.setCategory(testCategory);
        testRecord.setLabels(new HashSet<>(Arrays.asList(testLabel)));
        testRecord.setRecordDate(LocalDate.now());

        createRequest = new FinancialRecordCreateRequest();
        createRequest.setUserId(1L);
        createRequest.setAccountId(1L);
        createRequest.setAmount(BigDecimal.valueOf(200.00));
        createRequest.setCurrency("USD");
        createRequest.setRecordType(RecordType.EXPENSE);
        createRequest.setCategoryId(1L);
        createRequest.setLabelIds(new HashSet<>(Arrays.asList(1L)));
        createRequest.setRecordDate(LocalDate.now());
    }

    // ==================== createRecord() - EXPENSE Tests ====================

    @Test
    @DisplayName("createRecord - Should create expense and reduce account balance")
    void createRecord_WithExpense_ShouldReduceBalance() {
        // Given
        BigDecimal initialBalance = testAccount.getBalance();
        BigDecimal expenseAmount = createRequest.getAmount();

        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(labelRepository.findById(1L)).thenReturn(Optional.of(testLabel));
        when(financialRecordRepository.save(any(FinancialRecord.class))).thenReturn(testRecord);

        // When
        FinancialRecordResponse result = financialRecordService.createRecord(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(testAccount.getBalance())
                .isEqualByComparingTo(initialBalance.subtract(expenseAmount));
        verify(accountRepository, times(1)).save(testAccount);
        verify(financialRecordRepository, times(1)).save(any(FinancialRecord.class));
    }

    // ==================== createRecord() - INCOME Tests ====================

    @Test
    @DisplayName("createRecord - Should create income and increase account balance")
    void createRecord_WithIncome_ShouldIncreaseBalance() {
        // Given
        BigDecimal initialBalance = testAccount.getBalance();
        createRequest.setRecordType(RecordType.INCOME);
        BigDecimal incomeAmount = createRequest.getAmount();

        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(labelRepository.findById(1L)).thenReturn(Optional.of(testLabel));
        when(financialRecordRepository.save(any(FinancialRecord.class))).thenReturn(testRecord);

        // When
        FinancialRecordResponse result = financialRecordService.createRecord(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(testAccount.getBalance())
                .isEqualByComparingTo(initialBalance.add(incomeAmount));
        verify(accountRepository, times(1)).save(testAccount);
        verify(financialRecordRepository, times(1)).save(any(FinancialRecord.class));
    }

    // ==================== createRecord() - TRANSFER Tests ====================

    @Test
    @DisplayName("createRecord - Should create transfer and update both accounts")
    void createRecord_WithTransfer_ShouldUpdateBothAccounts() {
        // Given
        BigDecimal fromInitialBalance = testAccount.getBalance();
        BigDecimal toInitialBalance = toAccount.getBalance();
        BigDecimal transferAmount = createRequest.getAmount();

        createRequest.setRecordType(RecordType.TRANSFER);
        createRequest.setToAccountId(2L);

        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(labelRepository.findById(1L)).thenReturn(Optional.of(testLabel));
        when(financialRecordRepository.save(any(FinancialRecord.class))).thenReturn(testRecord);

        // When
        FinancialRecordResponse result = financialRecordService.createRecord(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(testAccount.getBalance())
                .isEqualByComparingTo(fromInitialBalance.subtract(transferAmount));
        assertThat(toAccount.getBalance())
                .isEqualByComparingTo(toInitialBalance.add(transferAmount));
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(financialRecordRepository, times(1)).save(any(FinancialRecord.class));
    }

    @Test
    @DisplayName("createRecord - Should throw exception when user does not exist")
    void createRecord_WithInvalidUser_ShouldThrowException() {
        // Given
        when(userService.getUserById(999L)).thenReturn(Optional.empty());
        createRequest.setUserId(999L);

        // When & Then
        assertThatThrownBy(() -> financialRecordService.createRecord(createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User with ID 999 does not exist");

        verify(financialRecordRepository, never()).save(any(FinancialRecord.class));
    }

    @Test
    @DisplayName("createRecord - Should throw exception when account does not exist")
    void createRecord_WithInvalidAccount_ShouldThrowException() {
        // Given
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());
        createRequest.setAccountId(999L);

        // When & Then
        assertThatThrownBy(() -> financialRecordService.createRecord(createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Account with ID 999 does not exist");

        verify(financialRecordRepository, never()).save(any(FinancialRecord.class));
    }

    @Test
    @DisplayName("createRecord - Should throw exception when account belongs to different user")
    void createRecord_WithAccountOfDifferentUser_ShouldThrowException() {
        // Given
        User anotherUser = new User();
        anotherUser.setId(2L);
        testAccount.setUser(anotherUser);

        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // When & Then
        assertThatThrownBy(() -> financialRecordService.createRecord(createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Account does not belong to the specified user");

        verify(financialRecordRepository, never()).save(any(FinancialRecord.class));
    }

    @Test
    @DisplayName("createRecord - Should throw exception when transfer missing toAccountId")
    void createRecord_TransferWithoutToAccount_ShouldThrowException() {
        // Given
        createRequest.setRecordType(RecordType.TRANSFER);
        createRequest.setToAccountId(null);

        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // When & Then
        assertThatThrownBy(() -> financialRecordService.createRecord(createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("To account is required for transfer records");

        verify(financialRecordRepository, never()).save(any(FinancialRecord.class));
    }

    @Test
    @DisplayName("createRecord - Should throw exception when transferring to same account")
    void createRecord_TransferToSameAccount_ShouldThrowException() {
        // Given
        createRequest.setRecordType(RecordType.TRANSFER);
        createRequest.setAccountId(1L);
        createRequest.setToAccountId(1L);

        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // When & Then
        assertThatThrownBy(() -> financialRecordService.createRecord(createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot transfer to the same account");

        verify(financialRecordRepository, never()).save(any(FinancialRecord.class));
    }

    // ==================== updateRecord() Tests ====================

    @Test
    @DisplayName("updateRecord - Should rollback old balance and apply new balance")
    void updateRecord_ShouldRollbackAndApplyNewBalance() {
        // Given
        // Initial state: Account has 1000, record was expense of 100, so balance is already 900
        testAccount.setBalance(BigDecimal.valueOf(900.00));
        testRecord.setAmount(BigDecimal.valueOf(100.00));
        testRecord.setRecordType(RecordType.EXPENSE);

        FinancialRecordUpdateRequest updateRequest = new FinancialRecordUpdateRequest();
        updateRequest.setAmount(BigDecimal.valueOf(150.00)); // Change expense from 100 to 150

        when(financialRecordRepository.findById(1L)).thenReturn(Optional.of(testRecord));
        when(financialRecordRepository.save(any(FinancialRecord.class))).thenReturn(testRecord);

        // When
        FinancialRecordResponse result = financialRecordService.updateRecord(1L, updateRequest);

        // Then
        // Balance should be: 900 + 100 (rollback) - 150 (new expense) = 850
        assertThat(testAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(850.00));
        verify(accountRepository, times(2)).save(testAccount); // Once for rollback, once for new
        verify(financialRecordRepository, times(1)).save(testRecord);
    }

    @Test
    @DisplayName("updateRecord - Should update amount successfully")
    void updateRecord_WithNewAmount_ShouldUpdateAmount() {
        // Given
        testAccount.setBalance(BigDecimal.valueOf(900.00));

        FinancialRecordUpdateRequest updateRequest = new FinancialRecordUpdateRequest();
        updateRequest.setAmount(BigDecimal.valueOf(200.00));

        when(financialRecordRepository.findById(1L)).thenReturn(Optional.of(testRecord));
        when(financialRecordRepository.save(any(FinancialRecord.class))).thenReturn(testRecord);

        // When
        financialRecordService.updateRecord(1L, updateRequest);

        // Then
        assertThat(testRecord.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(200.00));
        verify(financialRecordRepository, times(1)).save(testRecord);
    }

    @Test
    @DisplayName("updateRecord - Should throw exception when record not found")
    void updateRecord_WhenRecordNotFound_ShouldThrowException() {
        // Given
        FinancialRecordUpdateRequest updateRequest = new FinancialRecordUpdateRequest();

        when(financialRecordRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> financialRecordService.updateRecord(999L, updateRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Financial record with ID 999 does not exist");

        verify(financialRecordRepository, never()).save(any(FinancialRecord.class));
    }

    // ==================== deleteRecord() Tests ====================

    @Test
    @DisplayName("deleteRecord - Should rollback expense balance change")
    void deleteRecord_WithExpense_ShouldRollbackBalance() {
        // Given
        // Account balance is 900 after expense of 100 was created
        testAccount.setBalance(BigDecimal.valueOf(900.00));
        testRecord.setAmount(BigDecimal.valueOf(100.00));
        testRecord.setRecordType(RecordType.EXPENSE);

        when(financialRecordRepository.findById(1L)).thenReturn(Optional.of(testRecord));
        doNothing().when(financialRecordRepository).delete(testRecord);

        // When
        financialRecordService.deleteRecord(1L);

        // Then
        // Balance should be restored: 900 + 100 = 1000
        assertThat(testAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
        verify(accountRepository, times(1)).save(testAccount);
        verify(financialRecordRepository, times(1)).delete(testRecord);
    }

    @Test
    @DisplayName("deleteRecord - Should rollback income balance change")
    void deleteRecord_WithIncome_ShouldRollbackBalance() {
        // Given
        // Account balance is 1200 after income of 200 was added
        testAccount.setBalance(BigDecimal.valueOf(1200.00));
        testRecord.setAmount(BigDecimal.valueOf(200.00));
        testRecord.setRecordType(RecordType.INCOME);

        when(financialRecordRepository.findById(1L)).thenReturn(Optional.of(testRecord));
        doNothing().when(financialRecordRepository).delete(testRecord);

        // When
        financialRecordService.deleteRecord(1L);

        // Then
        // Balance should be restored: 1200 - 200 = 1000
        assertThat(testAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
        verify(accountRepository, times(1)).save(testAccount);
        verify(financialRecordRepository, times(1)).delete(testRecord);
    }

    @Test
    @DisplayName("deleteRecord - Should rollback transfer balance changes on both accounts")
    void deleteRecord_WithTransfer_ShouldRollbackBothAccounts() {
        // Given
        // From account: 1000 - 100 = 900 after transfer
        // To account: 5000 + 100 = 5100 after transfer
        testAccount.setBalance(BigDecimal.valueOf(900.00));
        toAccount.setBalance(BigDecimal.valueOf(5100.00));
        testRecord.setAmount(BigDecimal.valueOf(100.00));
        testRecord.setRecordType(RecordType.TRANSFER);
        testRecord.setToAccount(toAccount);

        when(financialRecordRepository.findById(1L)).thenReturn(Optional.of(testRecord));
        doNothing().when(financialRecordRepository).delete(testRecord);

        // When
        financialRecordService.deleteRecord(1L);

        // Then
        // From account: 900 + 100 = 1000
        // To account: 5100 - 100 = 5000
        assertThat(testAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
        assertThat(toAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(5000.00));
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(financialRecordRepository, times(1)).delete(testRecord);
    }

    @Test
    @DisplayName("deleteRecord - Should throw exception when record not found")
    void deleteRecord_WhenRecordNotFound_ShouldThrowException() {
        // Given
        when(financialRecordRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> financialRecordService.deleteRecord(999L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Financial record with ID 999 does not exist");

        verify(financialRecordRepository, never()).delete(any(FinancialRecord.class));
    }

    // ==================== getRecordById() Tests ====================

    @Test
    @DisplayName("getRecordById - Should return record when record exists")
    void getRecordById_WhenRecordExists_ShouldReturnRecord() {
        // Given
        when(financialRecordRepository.findById(1L)).thenReturn(Optional.of(testRecord));

        // When
        Optional<FinancialRecord> result = financialRecordService.getRecordById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(financialRecordRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getRecordById - Should return empty when record does not exist")
    void getRecordById_WhenRecordDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(financialRecordRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<FinancialRecord> result = financialRecordService.getRecordById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(financialRecordRepository, times(1)).findById(999L);
    }

    // ==================== getRecordsByUser() Tests ====================

    @Test
    @DisplayName("getRecordsByUser - Should return all records for a user")
    void getRecordsByUser_ShouldReturnAllRecords() {
        // Given
        when(financialRecordRepository.findByUserId(1L)).thenReturn(Arrays.asList(testRecord));

        // When
        List<FinancialRecordResponse> result = financialRecordService.getRecordsByUser(1L);

        // Then
        assertThat(result).hasSize(1);
        verify(financialRecordRepository, times(1)).findByUserId(1L);
    }

    // ==================== getRecordsByUserAndDateRange() Tests ====================

    @Test
    @DisplayName("getRecordsByUserAndDateRange - Should return records within date range")
    void getRecordsByUserAndDateRange_ShouldReturnFilteredRecords() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        when(financialRecordRepository.findByUserIdAndRecordDateBetween(1L, startDate, endDate))
                .thenReturn(Arrays.asList(testRecord));

        // When
        List<FinancialRecordResponse> result = financialRecordService.getRecordsByUserAndDateRange(1L, startDate, endDate);

        // Then
        assertThat(result).hasSize(1);
        verify(financialRecordRepository, times(1)).findByUserIdAndRecordDateBetween(1L, startDate, endDate);
    }

    // ==================== getRecordsByAccount() Tests ====================

    @Test
    @DisplayName("getRecordsByAccount - Should return records for specific account")
    void getRecordsByAccount_ShouldReturnAccountRecords() {
        // Given
        when(financialRecordRepository.findByAccountId(1L)).thenReturn(Arrays.asList(testRecord));

        // When
        List<FinancialRecordResponse> result = financialRecordService.getRecordsByAccount(1L);

        // Then
        assertThat(result).hasSize(1);
        verify(financialRecordRepository, times(1)).findByAccountId(1L);
    }

    // ==================== getRecordsByCategory() Tests ====================

    @Test
    @DisplayName("getRecordsByCategory - Should return records for specific category")
    void getRecordsByCategory_ShouldReturnCategoryRecords() {
        // Given
        when(financialRecordRepository.findByCategoryId(1L)).thenReturn(Arrays.asList(testRecord));

        // When
        List<FinancialRecordResponse> result = financialRecordService.getRecordsByCategory(1L);

        // Then
        assertThat(result).hasSize(1);
        verify(financialRecordRepository, times(1)).findByCategoryId(1L);
    }

    // ==================== getRecordsByType() Tests ====================

    @Test
    @DisplayName("getRecordsByType - Should return records filtered by type")
    void getRecordsByType_ShouldReturnFilteredRecords() {
        // Given
        when(financialRecordRepository.findByUserIdAndRecordType(1L, RecordType.EXPENSE))
                .thenReturn(Arrays.asList(testRecord));

        // When
        List<FinancialRecordResponse> result = financialRecordService.getRecordsByType(1L, RecordType.EXPENSE);

        // Then
        assertThat(result).hasSize(1);
        verify(financialRecordRepository, times(1)).findByUserIdAndRecordType(1L, RecordType.EXPENSE);
    }

    // ==================== mapToResponse() Tests ====================

    @Test
    @DisplayName("mapToResponse - Should map record entity to response DTO correctly")
    void mapToResponse_ShouldMapCorrectly() {
        // When
        FinancialRecordResponse result = financialRecordService.mapToResponse(testRecord);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testRecord.getId());
        assertThat(result.getUserId()).isEqualTo(testRecord.getUser().getId());
        assertThat(result.getAccountId()).isEqualTo(testRecord.getAccount().getId());
        assertThat(result.getAccountName()).isEqualTo(testRecord.getAccount().getName());
        assertThat(result.getAmount()).isEqualByComparingTo(testRecord.getAmount());
        assertThat(result.getRecordType()).isEqualTo(testRecord.getRecordType());
        assertThat(result.getCategoryId()).isEqualTo(testRecord.getCategory().getId());
    }

    @Test
    @DisplayName("mapToResponse - Should map transfer record with toAccount correctly")
    void mapToResponse_WithTransfer_ShouldIncludeToAccount() {
        // Given
        testRecord.setRecordType(RecordType.TRANSFER);
        testRecord.setToAccount(toAccount);

        // When
        FinancialRecordResponse result = financialRecordService.mapToResponse(testRecord);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToAccountId()).isEqualTo(toAccount.getId());
        assertThat(result.getToAccountName()).isEqualTo(toAccount.getName());
    }
}
