package com.ceylabs.fintrackerbackend.service;

import com.ceylabs.fintrackerbackend.dto.*;
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
public class TemplateService {

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private FinancialRecordService financialRecordService;

    // Create a new template
    @Transactional
    public TemplateResponse createTemplate(TemplateCreateRequest request) {
        // Validate user exists
        User user = userService.getUserById(request.getUserId());
        if (user == null) {
            throw new IllegalStateException("User with ID " + request.getUserId() + " does not exist");
        }

        // Check for duplicate template name for this user
        if (templateRepository.existsByNameAndUserId(request.getName(), request.getUserId())) {
            throw new IllegalStateException("Template with name '" + request.getName() + "' already exists for this user");
        }

        // Validate account if provided
        Account account = null;
        if (request.getAccountId() != null) {
            account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new IllegalStateException("Account with ID " + request.getAccountId() + " does not exist"));

            if (!account.getUser().getId().equals(request.getUserId())) {
                throw new IllegalStateException("Account does not belong to the specified user");
            }
        }

        // Validate to-account for transfers
        Account toAccount = null;
        if (request.getRecordType() == RecordType.TRANSFER) {
            if (request.getToAccountId() == null) {
                throw new IllegalStateException("To account is required for transfer templates");
            }
            toAccount = accountRepository.findById(request.getToAccountId())
                    .orElseThrow(() -> new IllegalStateException("To account with ID " + request.getToAccountId() + " does not exist"));

            if (!toAccount.getUser().getId().equals(request.getUserId())) {
                throw new IllegalStateException("To account does not belong to the specified user");
            }

            if (request.getAccountId() != null && request.getAccountId().equals(request.getToAccountId())) {
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

        // Create template entity
        Template template = new Template();
        template.setUser(user);
        template.setName(request.getName());
        template.setAmount(request.getAmount());
        template.setRecordType(request.getRecordType());
        template.setCategory(category);
        template.setLabels(labels);
        template.setAccount(account);
        template.setNote(request.getNote());
        template.setPaymentType(request.getPaymentType());
        template.setPayer(request.getPayer());
        template.setToAccount(toAccount);

        // Save template
        Template savedTemplate = templateRepository.save(template);

        return mapToResponse(savedTemplate);
    }

    // Update an existing template
    @Transactional
    public TemplateResponse updateTemplate(Long templateId, TemplateCreateRequest request) {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalStateException("Template with ID " + templateId + " does not exist"));

        // Validate template belongs to user
        if (!template.getUser().getId().equals(request.getUserId())) {
            throw new IllegalStateException("Template does not belong to the specified user");
        }

        // Check for duplicate name (excluding current template)
        Optional<Template> existingTemplate = templateRepository.findByNameAndUserId(request.getName(), request.getUserId());
        if (existingTemplate.isPresent() && !existingTemplate.get().getId().equals(templateId)) {
            throw new IllegalStateException("Template with name '" + request.getName() + "' already exists for this user");
        }

        // Validate account if provided
        Account account = null;
        if (request.getAccountId() != null) {
            account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new IllegalStateException("Account with ID " + request.getAccountId() + " does not exist"));
        }

        // Validate to-account for transfers
        Account toAccount = null;
        if (request.getRecordType() == RecordType.TRANSFER) {
            if (request.getToAccountId() == null) {
                throw new IllegalStateException("To account is required for transfer templates");
            }
            toAccount = accountRepository.findById(request.getToAccountId())
                    .orElseThrow(() -> new IllegalStateException("To account with ID " + request.getToAccountId() + " does not exist"));
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

        // Update fields
        template.setName(request.getName());
        template.setAmount(request.getAmount());
        template.setRecordType(request.getRecordType());
        template.setCategory(category);
        template.setLabels(labels);
        template.setAccount(account);
        template.setNote(request.getNote());
        template.setPaymentType(request.getPaymentType());
        template.setPayer(request.getPayer());
        template.setToAccount(toAccount);

        // Save updated template
        Template updatedTemplate = templateRepository.save(template);

        return mapToResponse(updatedTemplate);
    }

    // Delete a template
    @Transactional
    public void deleteTemplate(Long templateId) {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalStateException("Template with ID " + templateId + " does not exist"));

        // Note: Deleting a template does not affect financial records created from it
        // The templateId field in FinancialRecord will still reference this template

        templateRepository.delete(template);
    }

    // Create a financial record from a template
    @Transactional
    public FinancialRecordResponse createRecordFromTemplate(Long templateId, BigDecimal amount, LocalDate recordDate, Long accountIdOverride, Long toAccountIdOverride) {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalStateException("Template with ID " + templateId + " does not exist"));

        // Build FinancialRecordCreateRequest from template
        FinancialRecordCreateRequest request = new FinancialRecordCreateRequest();
        request.setUserId(template.getUser().getId());

        // Use override account or template account
        if (accountIdOverride != null) {
            request.setAccountId(accountIdOverride);
        } else if (template.getAccount() != null) {
            request.setAccountId(template.getAccount().getId());
        } else {
            throw new IllegalStateException("Account must be specified either in template or as override");
        }

        // Use provided amount or template amount
        if (amount != null) {
            request.setAmount(amount);
        } else if (template.getAmount() != null) {
            request.setAmount(template.getAmount());
        } else {
            throw new IllegalStateException("Amount must be specified either in template or as parameter");
        }

        // Get currency from account
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new IllegalStateException("Account not found"));
        request.setCurrency(account.getCurrency());

        request.setRecordType(template.getRecordType());

        if (template.getCategory() != null) {
            request.setCategoryId(template.getCategory().getId());
        }

        // Map labels
        if (!template.getLabels().isEmpty()) {
            Set<Long> labelIds = template.getLabels().stream()
                    .map(Label::getId)
                    .collect(Collectors.toSet());
            request.setLabelIds(labelIds);
        }

        request.setNote(template.getNote());
        request.setPaymentType(template.getPaymentType());
        request.setPayer(template.getPayer());
        request.setRecordDate(recordDate != null ? recordDate : LocalDate.now());

        // Handle transfer to-account
        if (template.getRecordType() == RecordType.TRANSFER) {
            if (toAccountIdOverride != null) {
                request.setToAccountId(toAccountIdOverride);
            } else if (template.getToAccount() != null) {
                request.setToAccountId(template.getToAccount().getId());
            } else {
                throw new IllegalStateException("To account must be specified for transfer");
            }
        }

        // Mark as created from template
        request.setCreatedFromTemplate(true);
        request.setTemplateId(templateId);

        // Create the financial record
        return financialRecordService.createRecord(request);
    }

    // Get template by ID
    public Optional<Template> getTemplateById(Long templateId) {
        return templateRepository.findById(templateId);
    }

    // Get all templates by user
    public List<TemplateResponse> getTemplatesByUser(Long userId) {
        return templateRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get templates by user and record type
    public List<TemplateResponse> getTemplatesByUserAndType(Long userId, RecordType recordType) {
        return templateRepository.findByUserIdAndRecordType(userId, recordType).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Search templates by name
    public List<TemplateResponse> searchTemplatesByName(Long userId, String searchTerm) {
        return templateRepository.findByUserIdAndNameContainingIgnoreCase(userId, searchTerm).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Get template usage count
    public Long getTemplateUsageCount(Long templateId) {
        return templateRepository.countTemplateUsage(templateId);
    }

    // Helper method: Map entity to response DTO
    public TemplateResponse mapToResponse(Template template) {
        TemplateResponse response = new TemplateResponse();
        response.setId(template.getId());
        response.setUserId(template.getUser().getId());
        response.setName(template.getName());
        response.setAmount(template.getAmount());
        response.setRecordType(template.getRecordType());

        if (template.getCategory() != null) {
            response.setCategoryId(template.getCategory().getId());
            response.setCategoryName(template.getCategory().getName());
        }

        // Map labels to LabelResponse
        Set<LabelResponse> labelResponses = template.getLabels().stream()
                .map(this::mapLabelToResponse)
                .collect(Collectors.toSet());
        response.setLabels(labelResponses);

        if (template.getAccount() != null) {
            response.setAccountId(template.getAccount().getId());
            response.setAccountName(template.getAccount().getName());
        }

        response.setNote(template.getNote());
        response.setPaymentType(template.getPaymentType());
        response.setPayer(template.getPayer());
        response.setCreatedDate(template.getCreatedDate());

        if (template.getToAccount() != null) {
            response.setToAccountId(template.getToAccount().getId());
            response.setToAccountName(template.getToAccount().getName());
        }

        // Get usage count
        response.setUsageCount(templateRepository.countTemplateUsage(template.getId()));

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
