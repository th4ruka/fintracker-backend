package com.ceylabs.fintrackerbackend.dto;

import com.ceylabs.fintrackerbackend.enums.PaymentStatus;
import com.ceylabs.fintrackerbackend.enums.PaymentType;
import com.ceylabs.fintrackerbackend.enums.RecordType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public class FinancialRecordCreateRequest {

//    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Account ID is required")
    private Long accountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3-letter code")
    private String currency;

    @NotNull(message = "Record type is required")
    private RecordType recordType;

    private Long categoryId; // Optional

    private Set<Long> labelIds; // Optional - set of label IDs

    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    private String note;

    @Size(max = 100, message = "Payer must not exceed 100 characters")
    private String payer;

    private PaymentType paymentType; // Optional

    private PaymentStatus paymentStatus = PaymentStatus.CLEARED;

    @NotNull(message = "Record date is required")
    private LocalDate recordDate;

    private LocalTime recordTime; // Optional, defaults to now

    // Transfer-specific field
    private Long toAccountId; // Required only if recordType is TRANSFER

    // Template-related fields
    private Boolean createdFromTemplate = false;

    private Long templateId; // Reference to template if created from one

    private Boolean canCreateTemplate = true;

    // Constructors
    public FinancialRecordCreateRequest() {}

    // Getters and Setters
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

    public Set<Long> getLabelIds() {
        return labelIds;
    }

    public void setLabelIds(Set<Long> labelIds) {
        this.labelIds = labelIds;
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

    public Long getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(Long toAccountId) {
        this.toAccountId = toAccountId;
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

    public Boolean getCanCreateTemplate() {
        return canCreateTemplate;
    }

    public void setCanCreateTemplate(Boolean canCreateTemplate) {
        this.canCreateTemplate = canCreateTemplate;
    }
}
