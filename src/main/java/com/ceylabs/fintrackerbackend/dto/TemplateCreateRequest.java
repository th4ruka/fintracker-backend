package com.ceylabs.fintrackerbackend.dto;

import com.ceylabs.fintrackerbackend.enums.PaymentType;
import com.ceylabs.fintrackerbackend.enums.RecordType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Set;

public class TemplateCreateRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Template name is required")
    @Size(min = 1, max = 255, message = "Template name must be between 1 and 255 characters")
    private String name;

    @DecimalMin(value = "0.0", message = "Amount must be positive")
    private BigDecimal amount; // Optional - can be set when using template

    @NotNull(message = "Record type is required")
    private RecordType recordType;

    private Long categoryId; // Optional

    private Set<Long> labelIds; // Optional - set of label IDs

    private Long accountId; // Optional - for pre-selecting account

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;

    private PaymentType paymentType; // Optional

    @Size(max = 100, message = "Payer must not exceed 100 characters")
    private String payer; // Optional - who paid/received

    // Transfer-specific field
    private Long toAccountId; // Required only if recordType is TRANSFER

    // Constructors
    public TemplateCreateRequest() {}

    // Getters and Setters
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

    public Set<Long> getLabelIds() {
        return labelIds;
    }

    public void setLabelIds(Set<Long> labelIds) {
        this.labelIds = labelIds;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
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

    public Long getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(Long toAccountId) {
        this.toAccountId = toAccountId;
    }
}
