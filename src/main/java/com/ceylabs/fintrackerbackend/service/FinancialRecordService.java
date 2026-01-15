package com.ceylabs.fintrackerbackend.service;

import com.ceylabs.fintrackerbackend.dto.FinancialRecordCreateRequest;
import com.ceylabs.fintrackerbackend.dto.FinancialRecordResponse;
import com.ceylabs.fintrackerbackend.dto.FinancialRecordUpdateRequest;
import com.ceylabs.fintrackerbackend.dto.LabelResponse;
import com.ceylabs.fintrackerbackend.enums.RecordType;
import com.ceylabs.fintrackerbackend.model.*;
import com.ceylabs.fintrackerbackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FinancialRecordService {

    @Autowired
    private FinancialRecordRepository financialRecordRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private UserService userService;

    // Create a new financial record with automatic balance updates
    @Transactional
    public FinancialRecordResponse createRecord(FinancialRecordCreateRequest request) {
        // Validate user exists
        User user = userService.getUserById(request.getUserId());
        if (user == null) {
            throw new IllegalStateException("User with ID " + request.getUserId() + " does not exist");
        }

        // Validate account exists
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new IllegalStateException("Account with ID " + request.getAccountId() + " does not exist"));

        // Validate account belongs to user
        if (!account.getUser().getId().equals(request.getUserId())) {
            throw new IllegalStateException("Account does not belong to the specified user");
        }

        // Validate transfer-specific fields
        Account toAccount = null;
        if (request.getRecordType() == RecordType.TRANSFER) {
            if (request.getToAccountId() == null) {
                throw new IllegalStateException("To account is required for transfer records");
            }
            toAccount = accountRepository.findById(request.getToAccountId())
                    .orElseThrow(() -> new IllegalStateException("To account with ID " + request.getToAccountId() + " does not exist"));

            // Validate to-account belongs to same user
            if (!toAccount.getUser().getId().equals(request.getUserId())) {
                throw new IllegalStateException("To account does not belong to the specified user");
            }

            if (account.getId().equals(toAccount.getId())) {
                throw new IllegalStateException("Cannot transfer to the same account");
            }
        }

        // Validate category if provided
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalStateException("Category with ID " + request.getCategoryId() + " does not exist"));
        }

        // Validate labels if provided
        Set<Label> labels = new HashSet<>();
        if (request.getLabelIds() != null && !request.getLabelIds().isEmpty()) {
            labels = request.getLabelIds().stream()
                    .map(labelId -> labelRepository.findById(labelId)
                            .orElseThrow(() -> new IllegalStateException("Label with ID " + labelId + " does not exist")))
                    .collect(Collectors.toSet());
        }

        // Create the financial record entity
        FinancialRecord record = new FinancialRecord();
        record.setUser(user);
        record.setAccount(account);
        record.setAmount(request.getAmount());
        record.setCurrency(request.getCurrency());
        record.setRecordType(request.getRecordType());
        record.setCategory(category);
        record.setLabels(labels);
        record.setNote(request.getNote());
        record.setPayer(request.getPayer());
        record.setPaymentType(request.getPaymentType());
        record.setPaymentStatus(request.getPaymentStatus());
        record.setRecordDate(request.getRecordDate());
        record.setRecordTime(request.getRecordTime());
        record.setToAccount(toAccount);
        record.setCreatedFromTemplate(request.getCreatedFromTemplate());
        record.setTemplateId(request.getTemplateId());
        record.setCanCreateTemplate(request.getCanCreateTemplate());

        // Update account balances based on record type
        updateAccountBalances(record, account, toAccount, true);

        // Save the record
        FinancialRecord savedRecord = financialRecordRepository.save(record);

        return mapToResponse(savedRecord);
    }

    // Update an existing financial record
    @Transactional
    public FinancialRecordResponse updateRecord(Long recordId, FinancialRecordUpdateRequest request) {
        FinancialRecord record = financialRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalStateException("Financial record with ID " + recordId + " does not exist"));

        // Store old values for balance adjustment
        BigDecimal oldAmount = record.getAmount();
        RecordType oldRecordType = record.getRecordType();
        Account oldFromAccount = record.getAccount();
        Account oldToAccount = record.getToAccount();

        // Rollback old balance changes
        updateAccountBalances(record, oldFromAccount, oldToAccount, false);

        // Update fields if provided
        if (request.getAmount() != null) {
            record.setAmount(request.getAmount());
        }
        if (request.getCurrency() != null) {
            record.setCurrency(request.getCurrency());
        }
        if (request.getRecordType() != null) {
            record.setRecordType(request.getRecordType());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalStateException("Category with ID " + request.getCategoryId() + " does not exist"));
            record.setCategory(category);
        }
        if (request.getLabelIds() != null) {
            Set<Label> labels = request.getLabelIds().stream()
                    .map(labelId -> labelRepository.findById(labelId)
                            .orElseThrow(() -> new IllegalStateException("Label with ID " + labelId + " does not exist")))
                    .collect(Collectors.toSet());
            record.setLabels(labels);
        }
        if (request.getNote() != null) {
            record.setNote(request.getNote());
        }
        if (request.getPayer() != null) {
            record.setPayer(request.getPayer());
        }
        if (request.getPaymentType() != null) {
            record.setPaymentType(request.getPaymentType());
        }
        if (request.getPaymentStatus() != null) {
            record.setPaymentStatus(request.getPaymentStatus());
        }
        if (request.getRecordDate() != null) {
            record.setRecordDate(request.getRecordDate());
        }
        if (request.getRecordTime() != null) {
            record.setRecordTime(request.getRecordTime());
        }
        if (request.getToAccountId() != null) {
            Account toAccount = accountRepository.findById(request.getToAccountId())
                    .orElseThrow(() -> new IllegalStateException("To account with ID " + request.getToAccountId() + " does not exist"));
            record.setToAccount(toAccount);
        }
        if (request.getCanCreateTemplate() != null) {
            record.setCanCreateTemplate(request.getCanCreateTemplate());
        }

        // Apply new balance changes
        updateAccountBalances(record, record.getAccount(), record.getToAccount(), true);

        // Save updated record
        FinancialRecord updatedRecord = financialRecordRepository.save(record);

        return mapToResponse(updatedRecord);
    }

    // Delete a financial record and rollback balance changes
    @Transactional
    public void deleteRecord(Long recordId) {
        FinancialRecord record = financialRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalStateException("Financial record with ID " + recordId + " does not exist"));

        // Rollback balance changes
        updateAccountBalances(record, record.getAccount(), record.getToAccount(), false);

        financialRecordRepository.delete(record);
    }

    // Get record by ID
    public Optional<FinancialRecord> getRecordById(Long recordId) {
        return financialRecordRepository.findById(recordId);
    }

    // Get all records by user
    public List<FinancialRecordResponse> getRecordsByUser(Long userId) {
        return financialRecordRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get records by user and date range
    public List<FinancialRecordResponse> getRecordsByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return financialRecordRepository.findByUserIdAndRecordDateBetween(userId, startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get records by account
    public List<FinancialRecordResponse> getRecordsByAccount(Long accountId) {
        return financialRecordRepository.findByAccountId(accountId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get records by category
    public List<FinancialRecordResponse> getRecordsByCategory(Long categoryId) {
        return financialRecordRepository.findByCategoryId(categoryId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get records by record type
    public List<FinancialRecordResponse> getRecordsByType(Long userId, RecordType recordType) {
        return financialRecordRepository.findByUserIdAndRecordType(userId, recordType).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Helper method: Update account balances based on record type
    private void updateAccountBalances(FinancialRecord record, Account fromAccount, Account toAccount, boolean isCreating) {
        BigDecimal amount = record.getAmount();
        RecordType recordType = record.getRecordType();

        // If deleting/rolling back, reverse the operation
        int multiplier = isCreating ? 1 : -1;

        switch (recordType) {
            case EXPENSE:
                // Expense reduces account balance
                fromAccount.setBalance(fromAccount.getBalance().subtract(amount.multiply(BigDecimal.valueOf(multiplier))));
                accountRepository.save(fromAccount);
                break;

            case INCOME:
                // Income increases account balance
                fromAccount.setBalance(fromAccount.getBalance().add(amount.multiply(BigDecimal.valueOf(multiplier))));
                accountRepository.save(fromAccount);
                break;

            case TRANSFER:
                // Transfer: debit from one account, credit to another
                if (toAccount == null) {
                    throw new IllegalStateException("To account is required for transfer records");
                }
                fromAccount.setBalance(fromAccount.getBalance().subtract(amount.multiply(BigDecimal.valueOf(multiplier))));
                toAccount.setBalance(toAccount.getBalance().add(amount.multiply(BigDecimal.valueOf(multiplier))));
                accountRepository.save(fromAccount);
                accountRepository.save(toAccount);
                break;
        }
    }

    // Helper method: Map entity to response DTO
    public FinancialRecordResponse mapToResponse(FinancialRecord record) {
        FinancialRecordResponse response = new FinancialRecordResponse();
        response.setId(record.getId());
        response.setUserId(record.getUser().getId());
        response.setAccountId(record.getAccount().getId());
        response.setAccountName(record.getAccount().getName());
        response.setAmount(record.getAmount());
        response.setCurrency(record.getCurrency());
        response.setRecordType(record.getRecordType());

        if (record.getCategory() != null) {
            response.setCategoryId(record.getCategory().getId());
            response.setCategoryName(record.getCategory().getName());
        }

        // Map labels to LabelResponse
        Set<LabelResponse> labelResponses = record.getLabels().stream()
                .map(this::mapLabelToResponse)
                .collect(Collectors.toSet());
        response.setLabels(labelResponses);

        response.setNote(record.getNote());
        response.setPayer(record.getPayer());
        response.setPaymentType(record.getPaymentType());
        response.setPaymentStatus(record.getPaymentStatus());
        response.setRecordDate(record.getRecordDate());
        response.setRecordTime(record.getRecordTime());
        response.setCreatedDate(record.getCreatedDate());
        response.setUpdatedDate(record.getUpdatedDate());

        if (record.getToAccount() != null) {
            response.setToAccountId(record.getToAccount().getId());
            response.setToAccountName(record.getToAccount().getName());
        }

        response.setCreatedFromTemplate(record.getCreatedFromTemplate());
        response.setTemplateId(record.getTemplateId());

        // Get template name if created from template
        if (record.getTemplateId() != null) {
            templateRepository.findById(record.getTemplateId()).ifPresent(template ->
                response.setTemplateName(template.getName())
            );
        }

        response.setCanCreateTemplate(record.getCanCreateTemplate());

        return response;
    }

    // Helper method: Map Label entity to LabelResponse
    private LabelResponse mapLabelToResponse(Label label) {
        LabelResponse response = new LabelResponse();
        response.setId(label.getId());
        response.setUserId(label.getUser().getId());
        response.setName(label.getName());
        response.setColor(label.getColor());
        response.setCreatedDate(label.getCreatedDate());
        return response;
    }
}
