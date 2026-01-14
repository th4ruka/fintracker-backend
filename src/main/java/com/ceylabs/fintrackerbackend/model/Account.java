package com.ceylabs.fintrackerbackend.model;


import com.ceylabs.fintrackerbackend.enums.AccountType;
import com.ceylabs.fintrackerbackend.enums.CreditBalanceType;
import com.ceylabs.fintrackerbackend.enums.OverdraftBalanceType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Account name is required")
    @Size(min = 1, max = 255)
    private String name;

    @NotNull(message = "Balance is required")
    private BigDecimal balance;

    private LocalDate createdDate;

    // Common fields for all account types
    @Column(length = 7, nullable = false)
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be valid hex code")
    private String color = "#000000";

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private AccountType accountType = AccountType.GENERAL;

    @Column(name = "initial_amount", nullable = false, precision = 19, scale = 2)
    @DecimalMin(value = "0.0", message = "Initial amount cannot be negative")
    private BigDecimal initialAmount = BigDecimal.ZERO;

    @Column(length = 3, nullable = false)
    @Size(min = 3, max = 3, message = "Currency must be 3-letter code")
    private String currency;

    @Column(name = "exclude_from_statistics", nullable = false)
    private Boolean excludeFromStatistics = false;

    // Credit account fields
    @Column(name = "credit_card_limit", precision = 19, scale = 2)
    @DecimalMin(value = "0.0", message = "Credit limit must be positive")
    private BigDecimal creditCardLimit;

    @Column(name = "credit_due_day_of_month")
    @Min(1) @Max(31)
    private Integer creditDueDayOfMonth;

    @Enumerated(EnumType.STRING)
    @Column(name = "credit_balance_type", length = 20, columnDefinition = "VARCHAR(20)")
    private CreditBalanceType creditBalanceType;

    // Overdraft account fields
    @Column(name = "overdraft_limit", precision = 19, scale = 2)
    @DecimalMin(value = "0.0", message = "Overdraft limit must be positive")
    private BigDecimal overdraftLimit;

    @Column(name = "overdraft_due_day_of_month")
    @Min(1) @Max(31)
    private Integer overdraftDueDayOfMonth;

    @Enumerated(EnumType.STRING)
    @Column(name = "overdraft_balance_type", length = 20, columnDefinition = "VARCHAR(20)")
    private OverdraftBalanceType overdraftBalanceType;

    // Relationships
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<FinancialRecord> financialRecords = new java.util.ArrayList<>();

    // Constructors
    public Account() {}

    public Account(String name, BigDecimal balance, User user) {
        this.name = name;
        this.balance = balance;
        this.user = user;
        this.createdDate = LocalDate.now();
        this.initialAmount = balance;
        this.accountType = AccountType.GENERAL;
        this.color = "#000000";
        this.excludeFromStatistics = false;
        // currency will be set by service layer from config
    }

    // Getters and Setters
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

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public BigDecimal getInitialAmount() {
        return initialAmount;
    }

    public void setInitialAmount(BigDecimal initialAmount) {
        this.initialAmount = initialAmount;
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

    public java.util.List<FinancialRecord> getFinancialRecords() {
        return financialRecords;
    }

    public void setFinancialRecords(java.util.List<FinancialRecord> financialRecords) {
        this.financialRecords = financialRecords;
    }

    @PrePersist
    @PreUpdate
    private void validateAccountTypeSpecificFields() {
        if (accountType == AccountType.CREDIT_ACCOUNT) {
            if (creditCardLimit == null) {
                throw new IllegalStateException("Credit limit required for credit accounts");
            }
            // Clear overdraft fields
            overdraftLimit = null;
            overdraftDueDayOfMonth = null;
            overdraftBalanceType = null;
        } else if (accountType == AccountType.OVERDRAFT_ACCOUNT) {
            if (overdraftLimit == null) {
                throw new IllegalStateException("Overdraft limit required for overdraft accounts");
            }
            // Clear credit fields
            creditCardLimit = null;
            creditDueDayOfMonth = null;
            creditBalanceType = null;
        } else { // GENERAL
            // Clear both type-specific fields
            creditCardLimit = null;
            creditDueDayOfMonth = null;
            creditBalanceType = null;
            overdraftLimit = null;
            overdraftDueDayOfMonth = null;
            overdraftBalanceType = null;
        }
    }
}
