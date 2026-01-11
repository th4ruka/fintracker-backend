package com.ceylabs.fintrackerbackend.model;

import com.ceylabs.fintrackerbackend.enums.PaymentStatus;
import com.ceylabs.fintrackerbackend.enums.PaymentType;
import com.ceylabs.fintrackerbackend.enums.RecordType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "financial_record")
public class FinancialRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3-letter code")
    @Column(length = 3, nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private RecordType recordType;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category; // Optional - can be uncategorized

    @ManyToMany
    @JoinTable(
        name = "financial_record_label",
        joinColumns = @JoinColumn(name = "record_id"),
        inverseJoinColumns = @JoinColumn(name = "label_id")
    )
    private Set<Label> labels = new HashSet<>();

    @Column(length = 1000)
    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    private String note;

    @Size(max = 100)
    @Column(length = 100)
    private String payer; // Who paid (for expense) or who paid you (for income)

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", length = 20, columnDefinition = "VARCHAR(20)")
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private PaymentStatus paymentStatus = PaymentStatus.CLEARED;

    @NotNull(message = "Record date is required")
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "record_time")
    private LocalTime recordTime;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    // Transfer-specific fields
    @ManyToOne
    @JoinColumn(name = "to_account_id")
    private Account toAccount; // Used only for TRANSFER type

    // Template-related fields
    @Column(name = "created_from_template")
    private Boolean createdFromTemplate = false;

    @Column(name = "template_id")
    private Long templateId; // Reference to template if created from one

    @Column(name = "can_create_template")
    private Boolean canCreateTemplate = true; // Allow creating template from this record

    // Constructors
    public FinancialRecord() {}

    public FinancialRecord(BigDecimal amount, String currency, RecordType recordType,
                          Account account, User user) {
        this.amount = amount;
        this.currency = currency;
        this.recordType = recordType;
        this.account = account;
        this.user = user;
        this.recordDate = LocalDate.now();
        this.recordTime = LocalTime.now();
        this.paymentStatus = PaymentStatus.CLEARED;
        this.createdFromTemplate = false;
        this.canCreateTemplate = true;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Set<Label> getLabels() {
        return labels;
    }

    public void setLabels(Set<Label> labels) {
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

    public Account getToAccount() {
        return toAccount;
    }

    public void setToAccount(Account toAccount) {
        this.toAccount = toAccount;
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

    @PrePersist
    private void onCreate() {
        // Set default values
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
        if (recordDate == null) {
            recordDate = LocalDate.now();
        }
        if (recordTime == null) {
            recordTime = LocalTime.now();
        }
        if (paymentStatus == null) {
            paymentStatus = PaymentStatus.CLEARED;
        }
        if (createdFromTemplate == null) {
            createdFromTemplate = false;
        }
        if (canCreateTemplate == null) {
            canCreateTemplate = true;
        }

        // Validate transfer fields
        validateTransferFields();

        // Validate category type
        validateCategoryType();
    }

    @PreUpdate
    private void onUpdate() {
        updatedDate = LocalDateTime.now();

        // Validate transfer fields
        validateTransferFields();

        // Validate category type
        validateCategoryType();
    }

    private void validateTransferFields() {
        if (recordType == RecordType.TRANSFER) {
            if (toAccount == null) {
                throw new IllegalStateException("To account is required for transfer records");
            }
            if (account == null) {
                throw new IllegalStateException("From account is required for transfer records");
            }
            if (account.getId() != null && account.getId().equals(toAccount.getId())) {
                throw new IllegalStateException("Cannot transfer to the same account");
            }
            // Category is optional for transfers but usually not used
        } else {
            // Clear toAccount for non-transfer types
            toAccount = null;
        }
    }

    private void validateCategoryType() {
        if (category != null) {
            // Validate category type matches record type
            if (recordType == RecordType.EXPENSE &&
                category.getCategoryType() == com.ceylabs.fintrackerbackend.enums.CategoryType.INCOME) {
                throw new IllegalStateException("Cannot use income category for expense record");
            }
            if (recordType == RecordType.INCOME &&
                category.getCategoryType() == com.ceylabs.fintrackerbackend.enums.CategoryType.EXPENSE) {
                throw new IllegalStateException("Cannot use expense category for income record");
            }
        }
    }
}
