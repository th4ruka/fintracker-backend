package com.ceylabs.fintrackerbackend.dto;

import com.ceylabs.fintrackerbackend.enums.AccountType;
import com.ceylabs.fintrackerbackend.enums.CreditBalanceType;
import com.ceylabs.fintrackerbackend.enums.OverdraftBalanceType;
import java.math.BigDecimal;
import java.time.LocalDate;

public class AccountResponse {
    private Long id;
    private String name;
    private BigDecimal balance;
    private BigDecimal initialAmount;
    private String color;
    private AccountType accountType;
    private String currency;
    private Boolean excludeFromStatistics;
    private LocalDate createdDate;
    private Long userId;

    // Credit fields
    private BigDecimal creditCardLimit;
    private Integer creditDueDayOfMonth;
    private CreditBalanceType creditBalanceType;

    // Overdraft fields
    private BigDecimal overdraftLimit;
    private Integer overdraftDueDayOfMonth;
    private OverdraftBalanceType overdraftBalanceType;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getInitialAmount() {
        return initialAmount;
    }

    public void setInitialAmount(BigDecimal initialAmount) {
        this.initialAmount = initialAmount;
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

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
