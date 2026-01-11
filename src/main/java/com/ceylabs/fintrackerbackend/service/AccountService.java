package com.ceylabs.fintrackerbackend.service;

import com.ceylabs.fintrackerbackend.dto.AccountCreateRequest;
import com.ceylabs.fintrackerbackend.dto.AccountResponse;
import com.ceylabs.fintrackerbackend.dto.AccountUpdateRequest;
import com.ceylabs.fintrackerbackend.enums.AccountType;
import com.ceylabs.fintrackerbackend.model.Account;
import com.ceylabs.fintrackerbackend.model.User;
import com.ceylabs.fintrackerbackend.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    private UserService userService;

    @Value("${app.defaults.currency}")
    private String defaultCurrency;

    public List<AccountResponse> getAccountsByUser(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    public AccountResponse createAccount(AccountCreateRequest request) {
        User user = userService.getUserById(request.getUserId());
        if (user == null) {
            throw new IllegalStateException("User with ID " + request.getUserId() + " does not exist");
        }

        Account account = mapCreateRequestToEntity(request, user);
        Account savedAccount = accountRepository.save(account);
        return mapEntityToResponse(savedAccount);
    }

    public AccountResponse updateAccount(Long accountId, AccountUpdateRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalStateException("Account not found: " + accountId));

        // Update common fields if provided
        if (request.getName() != null) account.setName(request.getName());
        if (request.getColor() != null) account.setColor(request.getColor());
        if (request.getCurrency() != null) account.setCurrency(request.getCurrency());
        if (request.getExcludeFromStatistics() != null)
            account.setExcludeFromStatistics(request.getExcludeFromStatistics());

        // Handle account type change
        if (request.getAccountType() != null) {
            account.setAccountType(request.getAccountType());
        }

        // Update type-specific fields based on current account type
        if (account.getAccountType() == AccountType.CREDIT_ACCOUNT) {
            if (request.getCreditCardLimit() != null)
                account.setCreditCardLimit(request.getCreditCardLimit());
            if (request.getCreditDueDayOfMonth() != null)
                account.setCreditDueDayOfMonth(request.getCreditDueDayOfMonth());
            if (request.getCreditBalanceType() != null)
                account.setCreditBalanceType(request.getCreditBalanceType());
        } else if (account.getAccountType() == AccountType.OVERDRAFT_ACCOUNT) {
            if (request.getOverdraftLimit() != null)
                account.setOverdraftLimit(request.getOverdraftLimit());
            if (request.getOverdraftDueDayOfMonth() != null)
                account.setOverdraftDueDayOfMonth(request.getOverdraftDueDayOfMonth());
            if (request.getOverdraftBalanceType() != null)
                account.setOverdraftBalanceType(request.getOverdraftBalanceType());
        }

        Account updatedAccount = accountRepository.save(account);
        return mapEntityToResponse(updatedAccount);
    }

    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }

    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }

    // Mapper methods
    private Account mapCreateRequestToEntity(AccountCreateRequest request, User user) {
        Account account = new Account();
        account.setName(request.getName());
        account.setBalance(request.getInitialAmount());
        account.setInitialAmount(request.getInitialAmount());
        account.setUser(user);
        account.setColor(request.getColor());
        account.setAccountType(request.getAccountType());
        account.setCurrency(request.getCurrency() != null ? request.getCurrency() : defaultCurrency);
        account.setExcludeFromStatistics(request.getExcludeFromStatistics());
        account.setCreatedDate(LocalDate.now());

        // Set type-specific fields
        if (request.getAccountType() == AccountType.CREDIT_ACCOUNT) {
            account.setCreditCardLimit(request.getCreditCardLimit());
            account.setCreditDueDayOfMonth(request.getCreditDueDayOfMonth());
            account.setCreditBalanceType(request.getCreditBalanceType());
        } else if (request.getAccountType() == AccountType.OVERDRAFT_ACCOUNT) {
            account.setOverdraftLimit(request.getOverdraftLimit());
            account.setOverdraftDueDayOfMonth(request.getOverdraftDueDayOfMonth());
            account.setOverdraftBalanceType(request.getOverdraftBalanceType());
        }

        return account;
    }

    public AccountResponse mapEntityToResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setName(account.getName());
        response.setBalance(account.getBalance());
        response.setInitialAmount(account.getInitialAmount());
        response.setColor(account.getColor());
        response.setAccountType(account.getAccountType());
        response.setCurrency(account.getCurrency());
        response.setExcludeFromStatistics(account.getExcludeFromStatistics());
        response.setCreatedDate(account.getCreatedDate());
        response.setUserId(account.getUser().getId());

        // Type-specific fields
        response.setCreditCardLimit(account.getCreditCardLimit());
        response.setCreditDueDayOfMonth(account.getCreditDueDayOfMonth());
        response.setCreditBalanceType(account.getCreditBalanceType());
        response.setOverdraftLimit(account.getOverdraftLimit());
        response.setOverdraftDueDayOfMonth(account.getOverdraftDueDayOfMonth());
        response.setOverdraftBalanceType(account.getOverdraftBalanceType());

        return response;
    }
}

