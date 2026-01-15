package com.ceylabs.fintrackerbackend.service;

import com.ceylabs.fintrackerbackend.dto.AccountCreateRequest;
import com.ceylabs.fintrackerbackend.dto.AccountResponse;
import com.ceylabs.fintrackerbackend.dto.AccountUpdateRequest;
import com.ceylabs.fintrackerbackend.enums.AccountType;
import com.ceylabs.fintrackerbackend.enums.BalanceType;
import com.ceylabs.fintrackerbackend.model.Account;
import com.ceylabs.fintrackerbackend.model.User;
import com.ceylabs.fintrackerbackend.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService Unit Tests")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AccountService accountService;

    private User testUser;
    private Account testAccount;
    private AccountCreateRequest createRequest;
    private AccountUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Set default currency via reflection
        ReflectionTestUtils.setField(accountService, "defaultCurrency", "USD");

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setName("Checking Account");
        testAccount.setBalance(BigDecimal.valueOf(1000.00));
        testAccount.setInitialAmount(BigDecimal.valueOf(1000.00));
        testAccount.setColor("#FF0000");
        testAccount.setAccountType(AccountType.BASIC);
        testAccount.setCurrency("USD");
        testAccount.setExcludeFromStatistics(false);
        testAccount.setUser(testUser);
        testAccount.setCreatedDate(LocalDate.now());

        createRequest = new AccountCreateRequest();
        createRequest.setName("Savings Account");
        createRequest.setInitialAmount(BigDecimal.valueOf(5000.00));
        createRequest.setColor("#00FF00");
        createRequest.setAccountType(AccountType.BASIC);
        createRequest.setUserId(1L);
        createRequest.setExcludeFromStatistics(false);

        updateRequest = new AccountUpdateRequest();
    }

    // ==================== getAccountsByUser() Tests ====================

    @Test
    @DisplayName("getAccountsByUser - Should return all accounts for a user")
    void getAccountsByUser_ShouldReturnAllAccounts() {
        // Given
        Account account2 = new Account();
        account2.setId(2L);
        account2.setName("Savings Account");
        account2.setBalance(BigDecimal.valueOf(5000.00));
        account2.setUser(testUser);
        account2.setAccountType(AccountType.BASIC);

        when(accountRepository.findByUserId(1L)).thenReturn(Arrays.asList(testAccount, account2));

        // When
        List<AccountResponse> result = accountService.getAccountsByUser(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Checking Account");
        assertThat(result.get(1).getName()).isEqualTo("Savings Account");
        verify(accountRepository, times(1)).findByUserId(1L);
    }

    @Test
    @DisplayName("getAccountsByUser - Should return empty list when user has no accounts")
    void getAccountsByUser_WhenNoAccounts_ShouldReturnEmptyList() {
        // Given
        when(accountRepository.findByUserId(1L)).thenReturn(Arrays.asList());

        // When
        List<AccountResponse> result = accountService.getAccountsByUser(1L);

        // Then
        assertThat(result).isEmpty();
        verify(accountRepository, times(1)).findByUserId(1L);
    }

    // ==================== createAccount() Tests ====================

    @Test
    @DisplayName("createAccount - Should create basic account successfully")
    void createAccount_WithBasicAccount_ShouldCreateAccount() {
        // Given
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        AccountResponse result = accountService.createAccount(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Checking Account");
        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
        verify(userService, times(1)).getUserById(1L);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("createAccount - Should create credit account with credit-specific fields")
    void createAccount_WithCreditAccount_ShouldCreateWithCreditFields() {
        // Given
        createRequest.setAccountType(AccountType.CREDIT_ACCOUNT);
        createRequest.setCreditCardLimit(BigDecimal.valueOf(10000.00));
        createRequest.setCreditDueDayOfMonth(15);
        createRequest.setCreditBalanceType(BalanceType.CREDIT);

        Account creditAccount = new Account();
        creditAccount.setId(1L);
        creditAccount.setName("Credit Card");
        creditAccount.setAccountType(AccountType.CREDIT_ACCOUNT);
        creditAccount.setCreditCardLimit(BigDecimal.valueOf(10000.00));
        creditAccount.setCreditDueDayOfMonth(15);
        creditAccount.setCreditBalanceType(BalanceType.CREDIT);
        creditAccount.setUser(testUser);

        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.save(any(Account.class))).thenReturn(creditAccount);

        // When
        AccountResponse result = accountService.createAccount(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccountType()).isEqualTo(AccountType.CREDIT_ACCOUNT);
        assertThat(result.getCreditCardLimit()).isEqualByComparingTo(BigDecimal.valueOf(10000.00));
        assertThat(result.getCreditDueDayOfMonth()).isEqualTo(15);
        assertThat(result.getCreditBalanceType()).isEqualTo(BalanceType.CREDIT);
        verify(userService, times(1)).getUserById(1L);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("createAccount - Should create overdraft account with overdraft-specific fields")
    void createAccount_WithOverdraftAccount_ShouldCreateWithOverdraftFields() {
        // Given
        createRequest.setAccountType(AccountType.OVERDRAFT_ACCOUNT);
        createRequest.setOverdraftLimit(BigDecimal.valueOf(5000.00));
        createRequest.setOverdraftDueDayOfMonth(20);
        createRequest.setOverdraftBalanceType(BalanceType.DEBIT);

        Account overdraftAccount = new Account();
        overdraftAccount.setId(1L);
        overdraftAccount.setName("Overdraft Account");
        overdraftAccount.setAccountType(AccountType.OVERDRAFT_ACCOUNT);
        overdraftAccount.setOverdraftLimit(BigDecimal.valueOf(5000.00));
        overdraftAccount.setOverdraftDueDayOfMonth(20);
        overdraftAccount.setOverdraftBalanceType(BalanceType.DEBIT);
        overdraftAccount.setUser(testUser);

        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.save(any(Account.class))).thenReturn(overdraftAccount);

        // When
        AccountResponse result = accountService.createAccount(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccountType()).isEqualTo(AccountType.OVERDRAFT_ACCOUNT);
        assertThat(result.getOverdraftLimit()).isEqualByComparingTo(BigDecimal.valueOf(5000.00));
        assertThat(result.getOverdraftDueDayOfMonth()).isEqualTo(20);
        assertThat(result.getOverdraftBalanceType()).isEqualTo(BalanceType.DEBIT);
        verify(userService, times(1)).getUserById(1L);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("createAccount - Should use default currency when currency not provided")
    void createAccount_WithNoCurrency_ShouldUseDefaultCurrency() {
        // Given
        createRequest.setCurrency(null);

        Account accountWithDefaultCurrency = new Account();
        accountWithDefaultCurrency.setId(1L);
        accountWithDefaultCurrency.setCurrency("USD");
        accountWithDefaultCurrency.setUser(testUser);

        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.save(any(Account.class))).thenReturn(accountWithDefaultCurrency);

        // When
        AccountResponse result = accountService.createAccount(createRequest);

        // Then
        assertThat(result.getCurrency()).isEqualTo("USD");
        verify(userService, times(1)).getUserById(1L);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("createAccount - Should throw exception when user does not exist")
    void createAccount_WithInvalidUser_ShouldThrowException() {
        // Given
        when(userService.getUserById(999L)).thenReturn(Optional.empty());
        createRequest.setUserId(999L);

        // When & Then
        assertThatThrownBy(() -> accountService.createAccount(createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User with ID 999 does not exist");

        verify(userService, times(1)).getUserById(999L);
        verify(accountRepository, never()).save(any(Account.class));
    }

    // ==================== updateAccount() Tests ====================

    @Test
    @DisplayName("updateAccount - Should update account name successfully")
    void updateAccount_WithNewName_ShouldUpdateName() {
        // Given
        updateRequest.setName("Updated Account Name");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        AccountResponse result = accountService.updateAccount(1L, updateRequest);

        // Then
        assertThat(testAccount.getName()).isEqualTo("Updated Account Name");
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    @DisplayName("updateAccount - Should update account color successfully")
    void updateAccount_WithNewColor_ShouldUpdateColor() {
        // Given
        updateRequest.setColor("#0000FF");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        AccountResponse result = accountService.updateAccount(1L, updateRequest);

        // Then
        assertThat(testAccount.getColor()).isEqualTo("#0000FF");
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    @DisplayName("updateAccount - Should update account currency successfully")
    void updateAccount_WithNewCurrency_ShouldUpdateCurrency() {
        // Given
        updateRequest.setCurrency("EUR");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        AccountResponse result = accountService.updateAccount(1L, updateRequest);

        // Then
        assertThat(testAccount.getCurrency()).isEqualTo("EUR");
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    @DisplayName("updateAccount - Should update excludeFromStatistics flag")
    void updateAccount_WithExcludeFlag_ShouldUpdateFlag() {
        // Given
        updateRequest.setExcludeFromStatistics(true);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        AccountResponse result = accountService.updateAccount(1L, updateRequest);

        // Then
        assertThat(testAccount.getExcludeFromStatistics()).isTrue();
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    @DisplayName("updateAccount - Should update credit account specific fields")
    void updateAccount_CreditAccountFields_ShouldUpdateCreditFields() {
        // Given
        testAccount.setAccountType(AccountType.CREDIT_ACCOUNT);
        updateRequest.setCreditCardLimit(BigDecimal.valueOf(15000.00));
        updateRequest.setCreditDueDayOfMonth(25);
        updateRequest.setCreditBalanceType(BalanceType.CREDIT);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        AccountResponse result = accountService.updateAccount(1L, updateRequest);

        // Then
        assertThat(testAccount.getCreditCardLimit()).isEqualByComparingTo(BigDecimal.valueOf(15000.00));
        assertThat(testAccount.getCreditDueDayOfMonth()).isEqualTo(25);
        assertThat(testAccount.getCreditBalanceType()).isEqualTo(BalanceType.CREDIT);
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    @DisplayName("updateAccount - Should update overdraft account specific fields")
    void updateAccount_OverdraftAccountFields_ShouldUpdateOverdraftFields() {
        // Given
        testAccount.setAccountType(AccountType.OVERDRAFT_ACCOUNT);
        updateRequest.setOverdraftLimit(BigDecimal.valueOf(8000.00));
        updateRequest.setOverdraftDueDayOfMonth(10);
        updateRequest.setOverdraftBalanceType(BalanceType.DEBIT);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        AccountResponse result = accountService.updateAccount(1L, updateRequest);

        // Then
        assertThat(testAccount.getOverdraftLimit()).isEqualByComparingTo(BigDecimal.valueOf(8000.00));
        assertThat(testAccount.getOverdraftDueDayOfMonth()).isEqualTo(10);
        assertThat(testAccount.getOverdraftBalanceType()).isEqualTo(BalanceType.DEBIT);
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    @DisplayName("updateAccount - Should change account type")
    void updateAccount_WithAccountTypeChange_ShouldUpdateType() {
        // Given
        updateRequest.setAccountType(AccountType.CREDIT_ACCOUNT);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        AccountResponse result = accountService.updateAccount(1L, updateRequest);

        // Then
        assertThat(testAccount.getAccountType()).isEqualTo(AccountType.CREDIT_ACCOUNT);
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    @DisplayName("updateAccount - Should throw exception when account not found")
    void updateAccount_WhenAccountNotFound_ShouldThrowException() {
        // Given
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> accountService.updateAccount(999L, updateRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Account not found: 999");

        verify(accountRepository, times(1)).findById(999L);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("updateAccount - Should not update fields when request values are null")
    void updateAccount_WithNullValues_ShouldNotUpdateFields() {
        // Given
        String originalName = testAccount.getName();
        String originalColor = testAccount.getColor();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        AccountResponse result = accountService.updateAccount(1L, updateRequest);

        // Then
        assertThat(testAccount.getName()).isEqualTo(originalName);
        assertThat(testAccount.getColor()).isEqualTo(originalColor);
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(testAccount);
    }

    // ==================== getAccountById() Tests ====================

    @Test
    @DisplayName("getAccountById - Should return account when account exists")
    void getAccountById_WhenAccountExists_ShouldReturnAccount() {
        // Given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // When
        Optional<Account> result = accountService.getAccountById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("Checking Account");
        verify(accountRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getAccountById - Should return empty optional when account does not exist")
    void getAccountById_WhenAccountDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Account> result = accountService.getAccountById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(accountRepository, times(1)).findById(999L);
    }

    // ==================== deleteAccount() Tests ====================

    @Test
    @DisplayName("deleteAccount - Should delete account successfully")
    void deleteAccount_ShouldDeleteAccount() {
        // Given
        doNothing().when(accountRepository).deleteById(1L);

        // When
        accountService.deleteAccount(1L);

        // Then
        verify(accountRepository, times(1)).deleteById(1L);
    }

    // ==================== mapEntityToResponse() Tests ====================

    @Test
    @DisplayName("mapEntityToResponse - Should map basic account correctly")
    void mapEntityToResponse_BasicAccount_ShouldMapCorrectly() {
        // When
        AccountResponse result = accountService.mapEntityToResponse(testAccount);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testAccount.getId());
        assertThat(result.getName()).isEqualTo(testAccount.getName());
        assertThat(result.getBalance()).isEqualByComparingTo(testAccount.getBalance());
        assertThat(result.getInitialAmount()).isEqualByComparingTo(testAccount.getInitialAmount());
        assertThat(result.getColor()).isEqualTo(testAccount.getColor());
        assertThat(result.getAccountType()).isEqualTo(testAccount.getAccountType());
        assertThat(result.getCurrency()).isEqualTo(testAccount.getCurrency());
        assertThat(result.getUserId()).isEqualTo(testAccount.getUser().getId());
    }

    @Test
    @DisplayName("mapEntityToResponse - Should map credit account with credit fields")
    void mapEntityToResponse_CreditAccount_ShouldIncludeCreditFields() {
        // Given
        testAccount.setAccountType(AccountType.CREDIT_ACCOUNT);
        testAccount.setCreditCardLimit(BigDecimal.valueOf(10000.00));
        testAccount.setCreditDueDayOfMonth(15);
        testAccount.setCreditBalanceType(BalanceType.CREDIT);

        // When
        AccountResponse result = accountService.mapEntityToResponse(testAccount);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCreditCardLimit()).isEqualByComparingTo(BigDecimal.valueOf(10000.00));
        assertThat(result.getCreditDueDayOfMonth()).isEqualTo(15);
        assertThat(result.getCreditBalanceType()).isEqualTo(BalanceType.CREDIT);
    }

    @Test
    @DisplayName("mapEntityToResponse - Should map overdraft account with overdraft fields")
    void mapEntityToResponse_OverdraftAccount_ShouldIncludeOverdraftFields() {
        // Given
        testAccount.setAccountType(AccountType.OVERDRAFT_ACCOUNT);
        testAccount.setOverdraftLimit(BigDecimal.valueOf(5000.00));
        testAccount.setOverdraftDueDayOfMonth(20);
        testAccount.setOverdraftBalanceType(BalanceType.DEBIT);

        // When
        AccountResponse result = accountService.mapEntityToResponse(testAccount);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOverdraftLimit()).isEqualByComparingTo(BigDecimal.valueOf(5000.00));
        assertThat(result.getOverdraftDueDayOfMonth()).isEqualTo(20);
        assertThat(result.getOverdraftBalanceType()).isEqualTo(BalanceType.DEBIT);
    }
}
