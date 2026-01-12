package com.ceylabs.fintrackerbackend.dto;

import com.ceylabs.fintrackerbackend.enums.PaymentType;
import com.ceylabs.fintrackerbackend.enums.RecordType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public class TemplateResponse {

    private Long id;
    private Long userId;
    private String name;
    private BigDecimal amount;
    private RecordType recordType;
    private Long categoryId;
    private String categoryName; // Include category name for convenience
    private Set<LabelResponse> labels; // Include full label details
    private Long accountId;
    private String accountName; // Include account name for convenience
    private String note;
    private PaymentType paymentType;
    private String payer;
    private LocalDate createdDate;

    // Transfer-specific fields
    private Long toAccountId;
    private String toAccountName; // Include to-account name for convenience

    // Usage statistics (optional, can be populated separately)
    private Long usageCount; // How many times this template has been used

    // Constructors
    public TemplateResponse() {}

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public String getPayer() {
        return payer;
    }

    public void setPayer(String payer) {
        this.payer = payer;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
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

    public Long getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Long usageCount) {
        this.usageCount = usageCount;
    }
}
