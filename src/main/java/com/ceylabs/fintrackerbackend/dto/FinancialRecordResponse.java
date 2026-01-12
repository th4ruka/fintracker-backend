package com.ceylabs.fintrackerbackend.dto;

import com.ceylabs.fintrackerbackend.enums.PaymentStatus;
import com.ceylabs.fintrackerbackend.enums.PaymentType;
import com.ceylabs.fintrackerbackend.enums.RecordType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

public class FinancialRecordResponse {

    private Long id;
    private Long userId;
    private Long accountId;
    private String accountName; // Include account name for convenience
    private BigDecimal amount;
    private String currency;
    private RecordType recordType;
    private Long categoryId;
    private String categoryName; // Include category name for convenience
    private Set<LabelResponse> labels; // Include full label details
    private String note;
    private String payer;
    private PaymentType paymentType;
    private PaymentStatus paymentStatus;
    private LocalDate recordDate;
    private LocalTime recordTime;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    // Transfer-specific fields
    private Long toAccountId;
    private String toAccountName; // Include to-account name for convenience

    // Template-related fields
    private Boolean createdFromTemplate;
    private Long templateId;
    private String templateName; // Include template name for convenience
    private Boolean canCreateTemplate;

    // Constructors
    public FinancialRecordResponse() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public RecordType getRecordType() {
        return recordType;
    }

    public void setRecordType(RecordType recordType) {
        this.recordType = recordType;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Set<LabelResponse> getLabels() {
        return labels;
    }

    public void setLabels(Set<LabelResponse> labels) {
        this.labels = labels;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPayer() {
        return payer;
    }

    public void setPayer(String payer) {
        this.payer = payer;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public LocalTime getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(LocalTime recordTime) {
        this.recordTime = recordTime;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Long getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(Long toAccountId) {
        this.toAccountId = toAccountId;
    }

    public String getToAccountName() {
        return toAccountName;
    }

    public void setToAccountName(String toAccountName) {
        this.toAccountName = toAccountName;
    }

    public Boolean getCreatedFromTemplate() {
        return createdFromTemplate;
    }

    public void setCreatedFromTemplate(Boolean createdFromTemplate) {
        this.createdFromTemplate = createdFromTemplate;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public Boolean getCanCreateTemplate() {
        return canCreateTemplate;
    }

    public void setCanCreateTemplate(Boolean canCreateTemplate) {
        this.canCreateTemplate = canCreateTemplate;
    }
}
