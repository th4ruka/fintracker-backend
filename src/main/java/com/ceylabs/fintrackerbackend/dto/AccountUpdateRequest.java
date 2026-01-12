package com.ceylabs.fintrackerbackend.dto;

import com.ceylabs.fintrackerbackend.enums.AccountType;
import com.ceylabs.fintrackerbackend.enums.CreditBalanceType;
import com.ceylabs.fintrackerbackend.enums.OverdraftBalanceType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class AccountUpdateRequest {

    @Size(min = 1, max = 255)
    private String name;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    private String color;

    private AccountType accountType;

    @Size(min = 3, max = 3)
    private String currency;

    private Boolean excludeFromStatistics;

    // Type-specific fields (all optional for updates)
    @DecimalMin(value = "0.0")
    private BigDecimal creditCardLimit;

    @Min(1) @Max(31)
    private Integer creditDueDayOfMonth;

    private CreditBalanceType creditBalanceType;

    @DecimalMin(value = "0.0")
    private BigDecimal overdraftLimit;

    @Min(1) @Max(31)
    private Integer overdraftDueDayOfMonth;

    private OverdraftBalanceType overdraftBalanceType;

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Boolean getExcludeFromStatistics() {
        return excludeFromStatistics;
    }

    public void setExcludeFromStatistics(Boolean excludeFromStatistics) {
        this.excludeFromStatistics = excludeFromStatistics;
    }

    public BigDecimal getCreditCardLimit() {
        return creditCardLimit;
    }

    public void setCreditCardLimit(BigDecimal creditCardLimit) {
        this.creditCardLimit = creditCardLimit;
    }

    public Integer getCreditDueDayOfMonth() {
        return creditDueDayOfMonth;
    }

    public void setCreditDueDayOfMonth(Integer creditDueDayOfMonth) {
        this.creditDueDayOfMonth = creditDueDayOfMonth;
    }

    public CreditBalanceType getCreditBalanceType() {
        return creditBalanceType;
    }

    public void setCreditBalanceType(CreditBalanceType creditBalanceType) {
        this.creditBalanceType = creditBalanceType;
    }

    public BigDecimal getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(BigDecimal overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    public Integer getOverdraftDueDayOfMonth() {
        return overdraftDueDayOfMonth;
    }

    public void setOverdraftDueDayOfMonth(Integer overdraftDueDayOfMonth) {
        this.overdraftDueDayOfMonth = overdraftDueDayOfMonth;
    }

    public OverdraftBalanceType getOverdraftBalanceType() {
        return overdraftBalanceType;
    }

    public void setOverdraftBalanceType(OverdraftBalanceType overdraftBalanceType) {
        this.overdraftBalanceType = overdraftBalanceType;
    }
}
