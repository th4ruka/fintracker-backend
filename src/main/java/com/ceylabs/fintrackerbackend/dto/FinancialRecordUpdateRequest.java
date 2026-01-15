package com.ceylabs.fintrackerbackend.dto;

import com.ceylabs.fintrackerbackend.enums.PaymentStatus;
import com.ceylabs.fintrackerbackend.enums.PaymentType;
import com.ceylabs.fintrackerbackend.enums.RecordType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public class FinancialRecordUpdateRequest {

    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Size(min = 3, max = 3, message = "Currency must be 3-letter code")
    private String currency;

    private RecordType recordType;

    private Long categoryId; // Set to null to remove category

    private Set<Long> labelIds; // Set to empty to remove all labels

    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    private String note;

    @Size(max = 100, message = "Payer must not exceed 100 characters")
    private String payer;

    private PaymentType paymentType;

    private PaymentStatus paymentStatus;

    private LocalDate recordDate;

    private LocalTime recordTime;

    // Transfer-specific field
    private Long toAccountId;

    private Boolean canCreateTemplate;

    // Constructors
    public FinancialRecordUpdateRequest() {}

    // Getters and Setters
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

    public Boolean getCanCreateTemplate() {
        return canCreateTemplate;
    }

    public void setCanCreateTemplate(Boolean canCreateTemplate) {
        this.canCreateTemplate = canCreateTemplate;
    }
}
